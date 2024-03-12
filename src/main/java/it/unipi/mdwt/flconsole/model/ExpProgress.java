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
@Document
public class ExpProgress {

    @Field("creationDate")
    @CreatedDate
    private Date creationDate;
    @Field("parameters")
    private Map<String, String> parameters;
    @Field("status")
    private String status;

}
