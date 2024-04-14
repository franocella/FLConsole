package it.unipi.mdwt.flconsole.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unipi.mdwt.flconsole.dto.ExpConfigSummary;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
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
    @Field("clientSelectionStrategy")
    private String clientSelectionStrategy;
    @Field("clientSelectionRatio")
    private Double clientSelectionRatio;
    @Field("minNumberClients")
    private Integer minNumberClients;
    @Field("stopCondition")
    private String stopCondition;
    @Field("stopConditionThreshold")
    private Double stopConditionThreshold;
    @Field("maxNumberOfRounds")
    private Integer maxNumberOfRounds;

    @Field("parameters")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
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
            jsonNode.put("clientSelectionStrategy", this.clientSelectionStrategy);
            jsonNode.put("clientSelectionRatio", this.clientSelectionRatio);
            jsonNode.put("minNumberClients", this.minNumberClients);
            jsonNode.put("stopCondition", this.stopCondition);
            jsonNode.put("stopConditionThreshold", this.stopConditionThreshold);
            jsonNode.put("maxNumberOfRounds", this.maxNumberOfRounds);

            ObjectNode parametersNode = objectMapper.createObjectNode();
            if (this.parameters != null){
                for (Map.Entry<String, String> entry : this.parameters.entrySet()) {
                    parametersNode.put(entry.getKey(), entry.getValue());
                }
                jsonNode.set("parameters", parametersNode);
            }
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
    public ExpConfigSummary toSummary() {
        ExpConfigSummary summary = new ExpConfigSummary();
        summary.setId(this.id);
        summary.setName(this.name);
        summary.setAlgorithm(this.algorithm);
        summary.setCreationDate(this.creationDate);
        return summary;
    }

}
