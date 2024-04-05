package it.unipi.mdwt.flconsole.service;

import com.ericsson.otp.erlang.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.mdwt.flconsole.dao.MetricsDao;
import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.ExpMetrics;
import it.unipi.mdwt.flconsole.model.Experiment;
import it.unipi.mdwt.flconsole.utils.ExperimentStatus;
import it.unipi.mdwt.flconsole.utils.MessageType;
import it.unipi.mdwt.flconsole.utils.Validator;
import it.unipi.mdwt.flconsole.utils.exceptions.messages.MessageException;
import it.unipi.mdwt.flconsole.utils.exceptions.messages.MessageTypeErrorsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;


import java.io.*;
import java.util.logging.Logger;

import static it.unipi.mdwt.flconsole.utils.Constants.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;


@Component
public class MessageService {

    private OtpNode webConsoleNode;
    private final Logger applicationLogger;
    private final SimpMessagingTemplate messagingTemplate;

    private final MetricsDao metricsDao;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MessageService(Logger applicationLogger, SimpMessagingTemplate messagingTemplate, MetricsDao metricsDao, MongoTemplate mongoTemplate) {
        this.applicationLogger = applicationLogger;
        this.messagingTemplate = messagingTemplate;
        this.metricsDao = metricsDao;
        this.mongoTemplate = mongoTemplate;
    }

    private OtpNode getWebConsoleNode(String email) throws IOException {
        if (webConsoleNode == null) {
            webConsoleNode = new OtpNode(Validator.getNameFromEmail(email), COOKIE);
        }
        return webConsoleNode;
    }

    public Pair<OtpNode, OtpMbox> sendRequest(String config, String email) throws IOException {

        // Get the instance of the webConsoleNode
        OtpNode webConsoleNode = getWebConsoleNode(email);
        applicationLogger.severe("Sender: WebConsole node created.");
        // Create a mailbox to send a request to the director
        OtpMbox mboxSender = webConsoleNode.createMbox("mboxSender");
        applicationLogger.severe("Sender: Mailbox created.");

        // Create the experiment node to handle the incoming messages
        OtpNode experimentNode = new OtpNode("experimentNode", COOKIE);
        applicationLogger.severe("Sender: Experiment node created.");

        // Create a mailbox to receive the request from the webConsole
        OtpMbox mboxReceiver = experimentNode.createMbox("mboxReceiver");
        applicationLogger.severe("Sender: Receiver mailbox created.");

        if (webConsoleNode.ping(DIRECTOR_NODE_NAME, 2000)) {
            System.out.println("Sender: Director node is up.");
        } else {
            System.out.println("Sender: Director node is down.");
            webConsoleNode.close();
            experimentNode.close();
            throw new IOException("Director node is down.");
        }

        // Create the message
        OtpErlangTuple message = createRequestMessage(mboxReceiver.self(), config);

        System.out.println("Sender: Sending message to the director...");
        mboxSender.send(DIRECTOR_MAILBOX, DIRECTOR_NODE_NAME, message);
        webConsoleNode.close();
        System.out.println("Sender: Sender closed.");
        return Pair.of(experimentNode, mboxReceiver);
    }

    private OtpErlangTuple createRequestMessage(OtpErlangPid receiverPid, String jsonConfig) {
        OtpErlangObject[] message = new OtpErlangObject[3];
        message[0] = new OtpErlangAtom("fl_start_str_run"); // message type
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ExpConfig expConfig = objectMapper.readValue(jsonConfig, ExpConfig.class);
            OtpErlangObject[] startStrRunMessage = new OtpErlangObject[9];
            startStrRunMessage[0] = new OtpErlangString(expConfig.getAlgorithm());
            startStrRunMessage[1] = new OtpErlangString(expConfig.getCodeLanguage());
            startStrRunMessage[2] = new OtpErlangString(expConfig.getStrategy());
            startStrRunMessage[3] = new OtpErlangDouble(expConfig.getClientSelectionRatio());
            startStrRunMessage[4] = new OtpErlangInt(expConfig.getMinNumClients());
            startStrRunMessage[5] = new OtpErlangString(expConfig.getStopCondition());
            startStrRunMessage[6] = new OtpErlangDouble(expConfig.getThreshold());
            startStrRunMessage[7] = new OtpErlangInt(expConfig.getMaxNumRounds());
            startStrRunMessage[8] = new OtpErlangString(objectMapper.writeValueAsString(expConfig.getParameters()));
            message[1] = new OtpErlangTuple(startStrRunMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        message[2] = receiverPid; // receiver pid
        return new OtpErlangTuple(message);
    }

    public void ackMessage(OtpMbox mboxReceiver, String expId) {
        try {
            System.out.println("Receiver: Waiting for ack message...");
            OtpErlangObject message = mboxReceiver.receive(10000);

            if (
                    message instanceof OtpErlangTuple tuple && tuple.arity() == 2 &&
                    tuple.elementAt(0) instanceof OtpErlangAtom atom && tuple.elementAt(1) instanceof OtpErlangString info &&
                    atom.atomValue().equals("fl_start_str_run")
            ) {
                String jsonMessage = info.stringValue();
                ObjectMapper objectMapper = new ObjectMapper();
                ExpMetrics expMetrics = objectMapper.readValue(jsonMessage, ExpMetrics.class);

                if (expMetrics.getType() == MessageType.EXPERIMENT_QUEUED) {
                    // try to send a message to the WebSocket topic
                    messagingTemplate.convertAndSend("/experiment/" + expId + "/metrics", jsonMessage);

                    // save the message into the database
                    expMetrics.setExpId(expId);
                    metricsDao.save(expMetrics);

                    // update the status of the experiment
                    Query query = new Query(where("id").is(expMetrics.getExpId()));
                    Update update = new Update().set("status", ExperimentStatus.QUEUED.toString());
                    mongoTemplate.updateFirst(query, update, Experiment.class);

                    System.out.println("Receiver: Received ack message.");
                } else {
                    System.out.println("Receiver: Invalid ack message type.");
                    throw new MessageException(MessageTypeErrorsEnum.INVALID_MESSAGE);
                }
            } else {
                System.out.println("Receiver: Invalid ack message format.");
                throw new MessageException(MessageTypeErrorsEnum.INVALID_MESSAGE);
            }

        } catch (OtpErlangExit e) {
            System.out.println("Receiver: No message received.");

        } catch (OtpErlangDecodeException e) {
            System.out.println("Receiver: Error decoding the message.");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void receiveMessage(Pair<OtpNode, OtpMbox> expNodeInfo, String expId) {
        while (true) {
            try {
                System.out.println("Receiver: Waiting for message...");
                OtpErlangObject message = expNodeInfo.getSecond().receive();

                if (
                        message instanceof OtpErlangTuple tuple && tuple.arity() == 2 &&
                                tuple.elementAt(0) instanceof OtpErlangAtom atom
                                && tuple.elementAt(1) instanceof OtpErlangString info &&
                                atom.atomValue().equals("fl_message")
                ) {

                    // take the json string from OtpErlangString and send it to the webConsole
                    String jsonMessage = info.stringValue();
                    messagingTemplate.convertAndSend("/experiment/" + expId + "/metrics", jsonMessage);

                    // deserialize and map the json message into ExpMetrics object
                    ObjectMapper objectMapper = new ObjectMapper();
                    ExpMetrics expMetrics = objectMapper.readValue(jsonMessage, ExpMetrics.class);

                    // save the message into the database
                    expMetrics.setExpId(expId);
                    metricsDao.save(expMetrics);

                    switch (expMetrics.getType()) {
                        case STRATEGY_SERVER_READY -> {
                            System.out.println("Receiver: Strategy server ready message received.");
                        }
                        case WORKER_READY -> {
                            System.out.println("Receiver: Worker ready message received.");
                        }
                        case ALL_WORKERS_READY -> {
                            System.out.println("Receiver: All workers ready message received.");
                        }
                        case START_ROUND -> {
                            // update the status of the experiment to running when the first round starts
                            if (expMetrics.getRound() == 1) {
                                Query query = new Query(where("id").is(expMetrics.getExpId()));
                                Update update = new Update().set("status", ExperimentStatus.RUNNING.toString());
                                mongoTemplate.updateFirst(query, update, Experiment.class);
                            }
                            System.out.println("Receiver: Start round message received.");
                        }
                        case WORKER_METRICS -> {
                            System.out.println("Receiver: Progress message received.");
                        }
                        case STRATEGY_SERVER_METRICS -> {
                            System.out.println("Receiver: Strategy server metrics message received.");
                        }
                        case END_ROUND -> {
                            System.out.println("Receiver: End round message received.");
                        }
                        default -> {
                            System.out.println("Receiver: Invalid message type.");
                        }
                    }
                } else if (
                        message instanceof OtpErlangTuple tuple && tuple.arity() == 3 &&
                                tuple.elementAt(0) instanceof OtpErlangAtom atom &&
                                atom.atomValue().equals("fl_end_str_run") &&
                                tuple.elementAt(1) instanceof OtpErlangString something &&
                                tuple.elementAt(2) instanceof OtpErlangBinary binary
                ) {

                    // save the model file in a specific directory
                    String filePath = saveFile(binary.binaryValue(), expId);

                    // update the status of the experiment to finished
                    Query query = new Query(where("id").is(expId));
                    Update update = new Update().set("status", ExperimentStatus.FINISHED.toString()).set("modelPath", filePath);
                    mongoTemplate.updateFirst(query, update, Experiment.class);

                    // send the end experiment message to the webConsole

                    String jsonMessage = "{\"type\":\"END_EXPERIMENT\"}";
                    messagingTemplate.convertAndSend("/experiment/" + expId + "/metrics", jsonMessage);

                    System.out.println("Receiver: End experiment message received.");

                    // close the mailboxes and the nodes
                    expNodeInfo.getFirst().close();
                    break;
                } else {
                    System.out.println("Receiver: Invalid message format.");
                }
            } catch (OtpErlangExit e) {
                System.out.println("Receiver: The experiment node has been closed.");
                break;
            } catch (OtpErlangDecodeException e) {
                System.out.println("Receiver: Error decoding the message.");
                break;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String saveFile(byte[] byteArray, String expId) {
        // Generates a unique name for the file
        String fileName = "exp_" + expId + ".bin";

        // Full path of the file
        String filePath = MODEL_PATH + File.separator + fileName;

        // Create a FileSystemResource for the file
        Resource resource = new FileSystemResource(filePath);

        // Write byte array to file
        try (FileOutputStream fos = new FileOutputStream(resource.getFile())) {
            fos.write(byteArray);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + filePath, e);
        }
        return filePath;
    }

}
