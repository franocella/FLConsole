package it.unipi.mdwt.flconsole.utils;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ExperimentStatus {

    NOT_STARTED,
    QUEUED,
    RUNNING,
    FINISHED,
    FAILED;

    @JsonCreator
    public static ExperimentStatus fromString(String value) {
        return ExperimentStatus.valueOf(value.toUpperCase());
    }
}
