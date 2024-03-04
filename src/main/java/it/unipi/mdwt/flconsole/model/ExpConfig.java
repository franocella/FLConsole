package it.unipi.mdwt.flconsole.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@Document(collection = "expConfig")
public class ExpConfig {
    @MongoId private String id;
    private String name;
    private String algorithm;
    private String strategy;
    private int numClients;
    private String stopCondition;
    private Double threshold;
    private Map<String,String> parameters;
    private LocalDate creationDate;
    private LocalDate lastUpdate;
}
