package it.unipi.mdwt.flconsole.service;

import com.ericsson.otp.erlang.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.mdwt.flconsole.dao.ExperimentDao;
import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.Experiment;
import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

@Service
public class ExperimentService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ExperimentDao experimentDao;

    private final Logger applicationLogger;

    @Autowired
    public ExperimentService(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper, ExperimentDao experimentDao,Logger applicationLogger) {
        this.messagingTemplate = messagingTemplate;
        this.experimentDao = experimentDao;
        this.applicationLogger = applicationLogger;
    }

    // Fake experiment to test WebSocket
    public void runExp() throws BusinessException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 10000;

        Random random = new Random();

        while (System.currentTimeMillis() < endTime) {
            int randomNumber = random.nextInt(100);

            try {
                // try to send a message to the WebSocket topic
                String jsonMessage = String.format("{\"%s\": %d}", "RandomValue", randomNumber);
                messagingTemplate.convertAndSend("/experiment/progress", jsonMessage);

            } catch (MessageDeliveryException e) {
                System.out.println("WebSocket connection is closed. Cannot send message.");
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void startExperiment(ExpConfig expConfig, String userId) {
        String nodeName = userId + "@localhost";
        String cookie = "start_exp";
        try {
            OtpNode otpNode = new OtpNode(nodeName, cookie);
            if (!otpNode.ping("directorNode@localhost", 2000)) {
                throw new Exception("Director is not responding");
            }
            OtpMbox otpMsgSender = otpNode.createMbox("exp_sender");
            OtpMbox otpMsgReceiver = otpNode.createMbox("exp_receiver");
            OtpErlangTuple startExpMessage = createStartExpMessage(expConfig, otpMsgSender.self(), otpMsgReceiver.self());
            otpMsgSender.send("expRequestHandler", "directorNode@localhost", startExpMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static void waitResponse(OtpMbox receiverMbox) {
        Thread thread = new Thread(() -> {
            try {
                OtpErlangPid collectorPid = null;
                while(true) {
                    if (collectorPid == null) {
                        OtpErlangObject receivedMessage = receiverMbox.receive(10000);

                        if (receivedMessage == null) {
                            throw new Exception("No response from the director");
                        } else if (receivedMessage instanceof OtpErlangTuple receivedTuple &&
                                receivedTuple.elementAt(0) instanceof OtpErlangAtom responseAtom) {
                            switch (responseAtom.atomValue()) {
                                case "ok" -> collectorPid = handleOkMessage(receivedTuple, receiverMbox);
                                case "error" -> {
                                    if (receivedTuple.elementAt(1) instanceof OtpErlangString errorMessage) {
                                        throw new Exception("Experiment start failed: " + errorMessage.stringValue());
                                    } else {
                                        throw new Exception("Experiment start failed: " + receivedTuple.elementAt(1));
                                    }
                                }
                                default -> throw new Exception("Received non-supported message: " + receivedMessage);
                            }
                        } else {
                            throw new Exception("Received non-supported message: " + receivedMessage);
                        }
                    }
                }

            } catch (OtpErlangDecodeException | OtpErlangExit ex) {
                throw new RuntimeException(ex);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        thread.start();
    }

    private static OtpErlangPid handleOkMessage(OtpErlangTuple receivedTuple, OtpMbox receiverMbox) {
        if (receivedTuple.elementAt(1) instanceof OtpErlangPid pid) {
            OtpErlangObject[] content = new OtpErlangObject[2];
            content[0] = receiverMbox.self();
            content[1] = new OtpErlangAtom("ack");
            OtpErlangTuple response = new OtpErlangTuple(content);
            receiverMbox.send(pid, response);
            try {
                OtpErlangObject otpErlangObject = receiverMbox.receive(5000);
                if (otpErlangObject instanceof OtpErlangTuple ackTuple &&
                        ackTuple.elementAt(0) instanceof OtpErlangPid msgPid &&
                        msgPid.equals(pid) &&
                        ackTuple.elementAt(1) instanceof OtpErlangAtom ackAtom &&
                        ackAtom.atomValue().equals("ack")
                        ) {
                    return pid;
                } else {
                    throw new Exception("No ack received from the collector");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            OtpErlangObject[] content = new OtpErlangObject[2];
            content[0] = receiverMbox.self();
            content[1] = new OtpErlangString("collectorPid not received");
            OtpErlangTuple response = new OtpErlangTuple(content);
            receiverMbox.send("expRequestHandler", "directorNode@localhost", response);
            try {
                OtpErlangObject otpErlangObject = receiverMbox.receive(5000);
                if (otpErlangObject instanceof OtpErlangTuple ackTuple &&
                        ackTuple.elementAt(0) instanceof OtpErlangAtom msgType &&
                        msgType.atomValue().equals("collector_id") &&
                        ackTuple.elementAt(1) instanceof OtpErlangPid collectorPid
                        ) {
                    return collectorPid;
                } else {
                    throw new Exception("Problem with acknowledgement mechanism");
                }
            } catch (OtpErlangExit | OtpErlangDecodeException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static OtpErlangTuple createStartExpMessage (ExpConfig expConfig, OtpErlangPid senderPid, OtpErlangPid receiverPid) {
        return new OtpErlangTuple(
                new OtpErlangObject[] {
                        senderPid,
                        receiverPid,
                        new OtpErlangString(expConfig.getName()),
                        new OtpErlangString(expConfig.getAlgorithm()),
                        new OtpErlangString(expConfig.getStrategy()),
                        new OtpErlangInt(expConfig.getNumClients()),
                        new OtpErlangString(expConfig.getStopCondition()),
                        new OtpErlangDouble(expConfig.getThreshold()),
                        new OtpErlangMap(
                                expConfig.getParameters().keySet().stream()
                                        .map(OtpErlangString::new)
                                        .toArray(OtpErlangObject[]::new),
                                expConfig.getParameters().values().stream()
                                        .map(OtpErlangString::new)
                                        .toArray(OtpErlangObject[]::new)
                        )
                }
        );
    }

    public Experiment getExpDetails(String id) throws BusinessException{
        Optional<Experiment> experiment;
        try {
            experiment = experimentDao.findById(id);
        } catch (Exception e) {
            applicationLogger.severe("An error occurred while fetching the experiment details: " + e.getMessage());
            throw new RuntimeException("An error occurred while fetching the experiment details");
        }
        return new Experiment();
    }


}
