package it.unipi.mdwt.flconsole.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@NoArgsConstructor
@Document
public class ExperimentSummary {
    @Id
    private String id;
    @Field("name")
    private String name;
    @Field("config")
    private String configName;

    @Field("creationDate")
    @CreatedDate
    private Date creationDate;

}
