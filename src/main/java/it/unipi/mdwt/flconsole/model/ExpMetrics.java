package it.unipi.mdwt.flconsole.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
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
    @Field("timestamp")
    private Date timestamp;
    @Field("parameters")
    private Map<String, String> parameters;
    @Field("status")
    private String status;

}