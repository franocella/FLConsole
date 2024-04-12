package it.unipi.mdwt.flconsole.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document()
public class UserSummary {

    @Field("email")
    @Indexed(unique = true)
    private String email;

    @Field("password")
    private String password;

    @Field("description")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String description;
}
