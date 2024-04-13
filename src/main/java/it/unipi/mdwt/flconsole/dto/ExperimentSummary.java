package it.unipi.mdwt.flconsole.dto;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@Document
public class ExperimentSummary {
    @Id
    private String id;
    @Field("name")
    private String name;
    @Field("config")
    private String configName;
    @Field("status")
    private String status;
    @Field("creationDate")
    @CreatedDate
    private Date creationDate;

}
