package it.unipi.mdwt.flconsole.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.bson.types.ObjectId;

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
}
