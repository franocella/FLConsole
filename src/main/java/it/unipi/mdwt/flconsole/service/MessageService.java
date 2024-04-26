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
import it.unipi.mdwt.flconsole.utils.exceptions.messages.MessageException;
import it.unipi.mdwt.flconsole.utils.exceptions.messages.MessageTypeErrorsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.logging.Logger;

import static it.unipi.mdwt.flconsole.utils.Constants.*;
import static it.unipi.mdwt.flconsole.utils.ValidatorAndSaver.saveFile;
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

    /**
     * Sends an experiment configuration to Erlang nodes and monitors the progress.
     *
     * @param config The experiment configuration in JSON format.
     * @param expId The ID of the experiment.
     */
    public void sendAndMonitor(String config, String expId) {
        OtpNode webConsoleNode = null;
        try {
            // Get the instance of the webConsoleNode
            webConsoleNode = new OtpNode(expId, COOKIE);

            applicationLogger.info("WebConsole node created.");
            // Create a mailbox to send a request to the director
            OtpMbox mboxSender = webConsoleNode.createMbox("mboxSender");

            if (webConsoleNode.ping(DIRECTOR_NODE_NAME, 2000)) {
                applicationLogger.info("Director node is up.");
            } else {
                applicationLogger.severe("Director node is down.");
                mboxSender.close();
                webConsoleNode.close();
                throw new MessageException(MessageTypeErrorsEnum.DIRECTOR_DOWN);
            }

            // Create the message
            OtpErlangTuple message = createRequestMessage(mboxSender.self(), config);
            applicationLogger.info("Sending the experiment request...");
            mboxSender.send(DIRECTOR_MAILBOX, DIRECTOR_NODE_NAME, message);
            applicationLogger.info("Experiment request sent.");

            // Wait for the ack message from the director
            OtpErlangObject erlMessage;
            applicationLogger.info("Waiting for ack message...");
            erlMessage = mboxSender.receive();
            if (
                    erlMessage instanceof OtpErlangTuple tuple && tuple.arity() == 2 &&
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

                    applicationLogger.info("Ack message received.");
                } else {
                    applicationLogger.severe("Invalid ack message type.");
                    throw new MessageException(MessageTypeErrorsEnum.INVALID_MESSAGE);
                }
            } else {
                applicationLogger.severe("Invalid ack message format.");
                throw new MessageException(MessageTypeErrorsEnum.INVALID_MESSAGE);
            }

            // Wait for messages from the Erlang nodes
            while (true) {
                try {
                    applicationLogger.info("Waiting for message...");
                    OtpErlangObject received = mboxSender.receive();
                    applicationLogger.info("Receiver: Message received: " + received.toString());

                    if (received instanceof OtpErlangTuple tuple2 && tuple2.elementAt(0) instanceof OtpErlangAtom atom4 &&
                            atom4.atomValue().equals("fl_end_str_run")){
                    }
                    if (
                            received instanceof OtpErlangTuple tuple2 && tuple2.arity() == 2 &&
                                    tuple2.elementAt(0) instanceof OtpErlangAtom atom2
                                    && tuple2.elementAt(1) instanceof OtpErlangString info2 &&
                                    atom2.atomValue().equals("fl_message")
                    ) {
                        // take the json string from OtpErlangString and send it to the webConsole
                        String jsonMessage = info2.stringValue();
                        messagingTemplate.convertAndSend("/experiment/" + expId + "/metrics", jsonMessage);
                        applicationLogger.severe("Message sent to the webConsole.");

                        // deserialize and map the json message into ExpMetrics object
                        ObjectMapper objectMapper = new ObjectMapper();
                        ExpMetrics expMetrics = objectMapper.readValue(jsonMessage, ExpMetrics.class);

                        // save the message into the database
                        expMetrics.setExpId(expId);
                        metricsDao.save(expMetrics);
                        applicationLogger.severe("Message saved into the database.");

                        switch (expMetrics.getType()) {
                            case STRATEGY_SERVER_READY -> applicationLogger.severe("Receiver: Strategy server ready message received.");
                            case WORKER_READY -> applicationLogger.severe("Receiver: Worker ready message received.");
                            case ALL_WORKERS_READY -> applicationLogger.severe("Receiver: All workers ready message received.");
                            case START_ROUND -> {
                                // update the status of the experiment to running when the first round starts
                                if (expMetrics.getRound() == 1) {
                                    Query query = new Query(where("id").is(expMetrics.getExpId()));
                                    Update update = new Update().set("status", ExperimentStatus.RUNNING.toString());
                                    mongoTemplate.updateFirst(query, update, Experiment.class);
                                }
                                applicationLogger.severe("Receiver: Start round message received.");
                            }
                            case WORKER_METRICS -> applicationLogger.severe("Receiver: Progress message received.");
                            case STRATEGY_SERVER_METRICS -> applicationLogger.severe("Receiver: Strategy server metrics message received.");
                            case END_ROUND -> applicationLogger.severe("Receiver: End round message received.");
                            default -> applicationLogger.severe("Receiver: Invalid message type.");
                        }

                    } else if (
                            received instanceof OtpErlangTuple tuple2 && tuple2.arity() == 3 &&
                                    tuple2.elementAt(0) instanceof OtpErlangAtom atom2 &&
                                    atom2.atomValue().equals("fl_end_str_run") &&
                                    tuple2.elementAt(1) instanceof OtpErlangString flExpId &&
                                    tuple2.elementAt(2) instanceof OtpErlangBinary binary
                    ) {

                        // save the model file in a specific directory
                        String filePath = saveFile(binary.binaryValue(), expId);

                        // Update the experiment document
                        Query query = new Query(where("id").is(expId));
                        Update update = new Update()
                                .set("flExpId", flExpId.toString())
                                .set("status", ExperimentStatus.FINISHED.toString())
                                .set("modelPath", filePath);
                        mongoTemplate.updateFirst(query, update, Experiment.class);
                        applicationLogger.severe("Updated experiment doc successfully with status, flExpId, and model path.");

                        // send the end experiment message to the webConsole
                        String jsonMessage = "{\"type\":\"END_EXPERIMENT\"}";
                        messagingTemplate.convertAndSend("/experiment/" + expId + "/metrics", jsonMessage);
                        applicationLogger.severe("End experiment message received.");

                        // close the mailboxes and the nodes
                        webConsoleNode.close();
                        break;
                    } else {

                        applicationLogger.severe("Invalid message format.");
                    }
                } catch (OtpErlangExit e) {
                    applicationLogger.severe("The experiment node has been closed.");
                    break;
                } catch (OtpErlangDecodeException e) {
                    applicationLogger.severe("Error decoding the message.");
                    break;
                } catch (JsonProcessingException e) {
                    applicationLogger.severe("Error decoding the message.");
                    throw new RuntimeException(e);
                }
            }

        } catch (IOException | OtpErlangDecodeException | OtpErlangExit e) {
            throw new RuntimeException(e);
        } finally {
            if(webConsoleNode != null) {
                webConsoleNode.close();
                applicationLogger.severe("WebConsoleNode closed.");
            }
        }
    }

    /**
     * Creates a request message to send to Erlang nodes.
     *
     * @param receiverPid The process ID of the receiver.
     * @param jsonConfig The experiment configuration in JSON format.
     * @return The created request message.
     */
    private OtpErlangTuple createRequestMessage(OtpErlangPid receiverPid, String jsonConfig) {
        OtpErlangObject[] message = new OtpErlangObject[3];
        message[0] = new OtpErlangAtom("fl_start_str_run"); // message type
        try {
            applicationLogger.severe("Creating the message...");
            ObjectMapper objectMapper = new ObjectMapper();

            //ExpConfig expConfig = objectMapper.readValue(jsonConfig, ExpConfig.class);
            ExpConfig expConfig = objectMapper.readValue(jsonConfig, ExpConfig.class);

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

        applicationLogger.severe("Message created. Receiver pid: " + receiverPid.toString());
        return new OtpErlangTuple(message);
    }

}
