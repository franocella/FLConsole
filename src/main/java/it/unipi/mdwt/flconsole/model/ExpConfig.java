package it.unipi.mdwt.flconsole.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@Document(collection = "expConfig")
public class ExpConfig {
    @Id
    private String id;
    @Field("name")
    private String name;
    @Field("algorithm")
    private String algorithm;
    @Field("codeLanguage")
    private String codeLanguage;
    @Field("strategy")
    private String strategy;
    @Field("clientSelectionRatio")
    private Double clientSelectionRatio;
    @Field("minNumClients")
    private int minNumClients;
    @Field("stopCondition")
    private String stopCondition;
    @Field("threshold")
    private Double threshold;
    @Field("maxNumRounds")
    private int maxNumRounds;
    @Field("parameters")
    private Map<String,String> parameters;
    @Field("creationDate")
    @CreatedDate
    private Date creationDate;



    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("id", this.id);
            jsonNode.put("name", this.name);
            jsonNode.put("algorithm", this.algorithm);
            jsonNode.put("codeLanguage", this.codeLanguage);
            jsonNode.put("strategy", this.strategy);
            jsonNode.put("clientSelectionRatio", this.clientSelectionRatio);
            jsonNode.put("minNumClients", this.minNumClients);
            jsonNode.put("stopCondition", this.stopCondition);
            jsonNode.put("threshold", this.threshold);
            jsonNode.put("maxNumRounds", this.maxNumRounds);
            jsonNode.put("creationDate", this.creationDate.toString());


            ObjectNode parametersNode = objectMapper.createObjectNode();
            for (Map.Entry<String, String> entry : this.parameters.entrySet()) {
                parametersNode.put(entry.getKey(), entry.getValue());
            }
            jsonNode.set("parameters", parametersNode);

            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
