package it.unipi.mdwt.flconsole.model;

import lombok.Data;
import lombok.NoArgsConstructor;;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;



@Data
@NoArgsConstructor
@Document
public class ExpConfigSummary {
    @Id
    private String id;
    @Field("name")
    private String name;
    @Field("algorithm")
    private String algorithm;
}
