package it.unipi.mdwt.flconsole.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private ObjectId id;

    @Field("email")
    @Indexed(unique = true)
    private String email;

    @Field("password")
    private String password;

    /*

    @Field("configurations")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ObjectId> configurations;

    @Field("experiments")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ObjectId> experiments;

    */

}
