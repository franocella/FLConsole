package it.unipi.mdwt.flconsole.dao;

import com.mongodb.DuplicateKeyException;
import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.utils.exceptions.dao.DaoException;
import it.unipi.mdwt.flconsole.utils.exceptions.dao.DaoTypeErrorsEnum;
import org.bson.types.ObjectId;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

@Repository
public interface UserDAO extends MongoRepository<User, String> {
    // No need to provide implementations for CRUD methods,
    // Spring Data MongoDB will automatically generate them.

    // Custom queries
    User findByEmail(String email);
    void deleteByEmail(String email);
    Boolean existsByEmail(String email);

    Boolean existsByEmailAndPassword(String email, String password);

    default User saveWithException(User user) throws DaoException {
        if (existsByEmail(user.getEmail())) {
            throw new DaoException(DaoTypeErrorsEnum.DUPLICATED_ELEMENT);
        }
        return save(user);
    }

        /*List<ExpConfig> findExpConfigsByConfigurations_Email(String userEmail);*/

}