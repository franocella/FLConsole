package it.unipi.mdwt.flconsole.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "experiments")
public class Experiment {
    @Id
    private String id;
    private String name;
    private ExpConfig config;

    @Field("creationDate")
    @CreatedDate
    private Date creationDate;

    @Field("lastUpdate")
    @LastModifiedDate
    private Date lastUpdate;

    @Field("expConfig")
    private ExpConfigSummary expConfigSummary;

    @Field("progressList")
    private List<ExpProgress> progressList;
}
