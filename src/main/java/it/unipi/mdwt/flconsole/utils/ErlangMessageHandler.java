package it.unipi.mdwt.flconsole.utils;

import com.ericsson.otp.erlang.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.mdwt.flconsole.model.ExpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static it.unipi.mdwt.flconsole.utils.Validator.getNameFromEmail;

@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS) // Ogni volta che viene iniettato un bean, ne viene creato uno nuovo
public class ErlangMessageHandler {

    private static final String DIRECTOR_NODE_NAME = "directorNode";
    private static final String DIRECTOR_MAILBOX = "expRequestHandler";
    private static final String COOKIE = "start_exp";
    private static SimpMessagingTemplate messagingTemplate;
    private static ObjectMapper objectMapper;
    private static OtpNode otpNode;

    @Autowired
    public ErlangMessageHandler(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        ErlangMessageHandler.messagingTemplate = messagingTemplate;
        ErlangMessageHandler.objectMapper = objectMapper;
    }

    // Inizializza il nodo Erlang se non esiste
    public void initialize(String email) {
        try {
            if (otpNode == null) {
                System.out.println("Creating new node");
                otpNode = new OtpNode(getNameFromEmail(email), COOKIE);
                System.out.println("Node created");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void killNodeIfNoMboxExist() {
        // If there are no mailboxes, the node is killed
        if (otpNode.getNames() == null || otpNode.getNames().length == 0) {
            otpNode.close();
            otpNode = null;
        }
    }

    // Invia un messaggio al nodo Erlang per avviare un esperimento
    // L'esperimento viene avviato solo se il nodo Erlang è attivo
    // Se l'esperimento è stato avviato, genera un thread per ricevere i messaggi di progresso
    public void startExperiment(String expConfig) {
        try {
            if (!otpNode.ping(DIRECTOR_NODE_NAME, 2000)) {
                throw new Exception("Director is not responding");
            }
            OtpMbox requestMailbox = otpNode.createMbox("exp_request");
            OtpMbox otpMsgReceiver = otpNode.createMbox("exp_receiver");
            OtpErlangObject[] content = new OtpErlangObject[2];
            content[0] = otpMsgReceiver.self();
            content[1] = new OtpErlangString(expConfig);
            OtpErlangTuple message = new OtpErlangTuple(content);
            System.out.println("Sending message to director");
            requestMailbox.send(DIRECTOR_MAILBOX, DIRECTOR_NODE_NAME, message);
            receiveProgress(otpMsgReceiver);
            requestMailbox.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Riceve i messaggi di progresso dall'esperimento
    // I messaggi di progresso vengono inviati al WebSocket
    // Se riceve un messaggio di stop, invia un messaggio di stop al WebSocket e chiude il nodo Erlang
    private void receiveProgress(OtpMbox receiverMbox) throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                while(true) {
                    System.out.println("Waiting for message...");
                    System.out.println("Receiver mbox: " + receiverMbox.self());
                    OtpErlangObject response = receiverMbox.receive();
                    if (response instanceof OtpErlangString jsonMessage) {
                        System.out.println("Received message: " + jsonMessage.stringValue());
                        Map<String, Object> message = objectMapper.readValue(jsonMessage.stringValue(), Map.class);
                        String type = (String) message.get("type");
                        System.out.println("Received message type: " + type);
                        if (type.equals("stop")) {
                            try {
                                System.out.println("Sending stop message to WebSocket");
                                messagingTemplate.convertAndSend("/experiment/progress", "{\"status\": \"stopped\"}");
                            } catch (MessageDeliveryException e) {
                                System.out.println("WebSocket connection is closed. Cannot send stop message.");
                            }
                            receiverMbox.close();
                            killNodeIfNoMboxExist();
                            break;
                        } else {
                            Map<String, String> parameters = (Map<String, String>) message.get("parameters");
                            System.out.println("Received message: " + parameters);
                            if (parameters != null) {
                                try {
                                    System.out.println("Sending progress message to WebSocket");
                                    messagingTemplate.convertAndSend("/experiment/progress", parameters);
                                } catch (MessageDeliveryException e) {
                                    System.out.println("WebSocket connection is closed. Cannot send progress message.");
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        thread.start();
    }
}


    /*    public String receiveMessage() {
        Random random = new Random();

        return "{"
                + "\"type\": \"data\","
                + "\"parameters\": {"
                + "\"param1\": " + random.nextInt(100) + ","
                + "\"param2\": " + random.nextInt(100)
                + "},"
                + "\"timestamp\": \"2024-03-13T12:34:56\","
                + "\"status\": \"running\""
                + "}";
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
     */

    /*
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
     */

