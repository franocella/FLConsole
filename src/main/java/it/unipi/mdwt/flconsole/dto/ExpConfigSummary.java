package it.unipi.mdwt.flconsole.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class ExpConfigSummary {
    @Id
    private String id;
    @Field("name")
    private String name;
    @Field("algorithm")
    private String algorithm;

    @Field("delete")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Boolean deleted;

    @Field("creationDate")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Date creationDate;


    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("id", this.id);
            jsonNode.put("name", this.name);
            jsonNode.put("algorithm", this.algorithm);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}


