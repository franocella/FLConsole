package it.unipi.mdwt.flconsole.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.unipi.mdwt.flconsole.utils.MessageType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@Document(collection = "expMetrics")
public class ExpMetrics {

    @Field("id")
    private String id;

    @Field("expId")
    private String expId;

    @Field("type")
    private MessageType type;

    @Field("hostMetrics")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Double> hostMetrics;

    @Field("modelMetrics")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Double> modelMetrics;

    @Field("timestamp")
    private Date timestamp;

    @Field("expStatus")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String expStatus;

    @Field("round")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Integer round;

    @Field("clientId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String clientId;

}

