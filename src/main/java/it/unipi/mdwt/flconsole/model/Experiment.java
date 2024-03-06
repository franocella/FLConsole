package it.unipi.mdwt.flconsole.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@NoArgsConstructor
@Document(collection = "experiments")
public class Experiment {
    @MongoId private String id;
    private String name;
    private ExpConfig config;

    // TODO: check other fields of experiment and update model
    private String status;
}
