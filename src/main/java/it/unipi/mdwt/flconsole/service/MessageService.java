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
import org.springframework.stereotype.Service;


import java.io.*;
import java.util.logging.Logger;

import static it.unipi.mdwt.flconsole.utils.Constants.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;


@Service
public class MessageService {

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


    public Pair<OtpNode, OtpMbox> sendRequest(String config, String email, String expId) {
        OtpNode webConsoleNode = null;
        try {
            // Get the instance of the webConsoleNode
            webConsoleNode = new OtpNode(Validator.getNameFromEmail(email), COOKIE);

            applicationLogger.severe("Sender: WebConsole node created.");
            // Create a mailbox to send a request to the director
            OtpMbox mboxSender = webConsoleNode.createMbox("mboxSender");
            applicationLogger.severe("Sender: Mailbox created.");

            // Create the experiment node to handle the incoming messages
            OtpNode experimentNode;
            try {
                experimentNode = new OtpNode(expId, COOKIE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            applicationLogger.severe("Sender: Experiment node created.");

            // Create a mailbox to receive the request from the webConsole
            OtpMbox mboxReceiver = experimentNode.createMbox();
            applicationLogger.severe("Sender: Receiver mailbox created.");

            if (webConsoleNode.ping(DIRECTOR_NODE_NAME, 2000)) {
                applicationLogger.severe("Sender: Director node is up.");
            } else {
                applicationLogger.severe("Sender: Director node is down.");
                mboxSender.close();
                mboxReceiver.close();
                webConsoleNode.close();
                experimentNode.close();
                throw new MessageException(MessageTypeErrorsEnum.DIRECTOR_DOWN);
            }

            // Create the message
            OtpErlangTuple message = createRequestMessage(mboxReceiver.self(), config);

            applicationLogger.severe("Sender: Sending the message...");
            mboxSender.send(DIRECTOR_MAILBOX, DIRECTOR_NODE_NAME, message);
            applicationLogger.severe("Sender: Message sent.");

            return Pair.of(experimentNode, mboxReceiver);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(webConsoleNode != null) {
                webConsoleNode.close();
                applicationLogger.severe("Sender: WebConsoleNode closed.");
            }
        }
    }

    private OtpErlangTuple createRequestMessage(OtpErlangPid receiverPid, String jsonConfig) {
        OtpErlangObject[] message = new OtpErlangObject[3];
        message[0] = new OtpErlangAtom("fl_start_str_run"); // message type
        try {
            applicationLogger.severe("Sender: Creating the message...");
            ObjectMapper objectMapper = new ObjectMapper();
            applicationLogger.severe("jsonConfig:" + jsonConfig);

            //ExpConfig expConfig = objectMapper.readValue(jsonConfig, ExpConfig.class);
            ExpConfig expConfig = objectMapper.readValue(jsonConfig, ExpConfig.class);

            applicationLogger.severe("Sender: Configuration deserialized.");
            OtpErlangObject[] startStrRunMessage = new OtpErlangObject[9];
            startStrRunMessage[0] = expConfig.getAlgorithm() != null ? new OtpErlangString(expConfig.getAlgorithm()) : new OtpErlangString("null");
            startStrRunMessage[1] = expConfig.getCodeLanguage() != null ? new OtpErlangString(expConfig.getCodeLanguage()) : new OtpErlangString("null");
            startStrRunMessage[2] = expConfig.getClientSelectionStrategy() != null ? new OtpErlangString(expConfig.getClientSelectionStrategy()) : new OtpErlangString("null");
            startStrRunMessage[3] = expConfig.getClientSelectionRatio() != null ? new OtpErlangDouble(expConfig.getClientSelectionRatio()) : new OtpErlangString("null");
            startStrRunMessage[4] = expConfig.getMinNumberClients() != null ? new OtpErlangInt(expConfig.getMinNumberClients()) : new OtpErlangString("null");
            startStrRunMessage[5] = expConfig.getStopCondition() != null ? new OtpErlangString(expConfig.getStopCondition()) : new OtpErlangString("null");
            startStrRunMessage[6] = expConfig.getStopConditionThreshold() != null ? new OtpErlangDouble(expConfig.getStopConditionThreshold()) : new OtpErlangString("null");
            startStrRunMessage[7] = expConfig.getMaxNumberOfRounds() != null ? new OtpErlangInt(expConfig.getMaxNumberOfRounds()) : new OtpErlangString("null");
            startStrRunMessage[8] = new OtpErlangString(expConfig.toJson());
            message[1] = new OtpErlangTuple(startStrRunMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        message[2] = receiverPid; // receiver pid
        applicationLogger.severe("Sender: Message created. Receiver pid: " + receiverPid.toString());
        return new OtpErlangTuple(message);
    }

    public void ackMessage(OtpMbox mboxReceiver, String expId) {
        try {
            applicationLogger.severe("Receiver: Waiting for ack message...");
            applicationLogger.severe("Receiver Pid:" + mboxReceiver.self().toString());
            OtpErlangObject message;
            message = mboxReceiver.receive();
            applicationLogger.severe("Receiver: Message received."+ message.toString());
            if (
                    message instanceof OtpErlangTuple tuple && tuple.arity() == 2 &&
                    tuple.elementAt(0) instanceof OtpErlangAtom atom && tuple.elementAt(1) instanceof OtpErlangString info &&
                    atom.atomValue().equals("fl_start_str_run")
            ) {
                String jsonMessage = info.stringValue();
                ObjectMapper objectMapper = new ObjectMapper();
                ExpMetrics expMetrics = objectMapper.readValue(jsonMessage, ExpMetrics.class);
                applicationLogger.severe("expMetrics: " + expMetrics.toString());

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
            applicationLogger.severe("Receiver: No message received.");

        } catch (OtpErlangDecodeException e) {
            applicationLogger.severe("Receiver: Error decoding the message.");

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);

        } catch (Exception e) {
            applicationLogger.severe("Receiver: Error receiving the message." + e.getMessage());
            e.printStackTrace();
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
