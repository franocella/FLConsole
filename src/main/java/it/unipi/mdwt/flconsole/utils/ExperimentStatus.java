package it.unipi.mdwt.flconsole.utils;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ExperimentStatus {
    NOT_STARTED,
    QUEUED,
    RUNNING,
    FINISHED;

    @JsonCreator
    public static ExperimentStatus fromString(String value) {
        return ExperimentStatus.valueOf(value.toUpperCase());
    }

    public String frontEndFormatted() {
        return switch (this) {
            case NOT_STARTED -> "Not Started";
            case QUEUED -> "Queued";
            case RUNNING -> "Running";
            case FINISHED -> "Finished";
        };
    }
}
