package it.unipi.mdwt.flconsole.utils;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Enumerates different types of messages used within the console application.
 */
public enum MessageType {
    EXPERIMENT_QUEUED,
    STRATEGY_SERVER_READY,
    WORKER_READY,
    ALL_WORKERS_READY,
    START_ROUND,
    WORKER_METRICS,
    STRATEGY_SERVER_METRICS,
    END_ROUND;

    @JsonCreator
    public static MessageType fromString(String value) {
        return MessageType.valueOf(value.toUpperCase());
    }
}
