package it.unipi.mdwt.flconsole.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@Document(collection = "expConfig")
public class ExpConfig {
    @Id
    private String id;
    @Field("name")
    private String name;
    @Field("algorithm")
    private String algorithm;
    @Field("strategy")
    private String strategy;
    @Field("numClients")
    private int numClients;
    @Field("stopCondition")
    private String stopCondition;
    @Field("threshold")
    private Double threshold;
    @Field("parameters")
    private Map<String,String> parameters;

    @Field("creationDate")
    @CreatedDate
    private Date creationDate;

    @Field("lastUpdate")
    @LastModifiedDate
    private Date lastUpdate;

}
