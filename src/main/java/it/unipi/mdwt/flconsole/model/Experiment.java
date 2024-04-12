package it.unipi.mdwt.flconsole.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.unipi.mdwt.flconsole.dto.ExpConfigSummary;
import it.unipi.mdwt.flconsole.utils.ExperimentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
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
    private ExpConfigSummary expConfig;

    @Field("creationDate")
    @CreatedDate
    private Date creationDate;

    @Field("metricsList")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> metricsList;

    @Field("status")
    private ExperimentStatus status = ExperimentStatus.NOT_STARTED;

    @Field("modelPath")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String modelPath;

    @Field("flExpId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String flExpId;
}

