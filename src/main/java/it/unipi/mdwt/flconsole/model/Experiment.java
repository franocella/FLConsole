package it.unipi.mdwt.flconsole.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @Field("name")
    private String name;

    @Field("expConfig")
    private ExpConfigSummary expConfigSummary;

    @Field("creationDate")
    @CreatedDate
    private Date creationDate;

    @Field("lastUpdate")
    @LastModifiedDate
    private Date lastUpdate;

    @Field("progressList")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ExpProgress> progressList;
}

