package it.unipi.mdwt.flconsole.utils;

/**
 * Enumerates different types of messages used within the console application.
 */
public enum MessageType {

    /**
     * Indicates that an experiment has been queued.
     */
    EXPERIMENT_QUEUED,

    /**
     * Indicates that the strategy server is ready.
     */
    STRATEGY_SERVER_READY,

    /**
     * Indicates that all workers are ready.
     */
    ALL_WORKERS_READY,

    /**
     * Indicates that the message contains metrics from the strategy server.
     */
    STRATEGY_SERVER_METRICS,

    /**
     * Indicates that the message contains metrics from a worker.
     */
    WORKER_METRICS,

    /**
     * Indicates the start of a round.
     */
    START_ROUND,

    /**
     * Indicates the end of a round.
     */
    END_ROUND,

    /**
     * Indicates the end of an experiment.
     */
    END_EXPERIMENT
}
