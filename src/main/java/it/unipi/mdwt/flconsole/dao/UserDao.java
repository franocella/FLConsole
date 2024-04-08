package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.utils.exceptions.dao.DaoException;
import it.unipi.mdwt.flconsole.utils.exceptions.dao.DaoTypeErrorsEnum;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collections;
import java.util.List;

@Repository
public interface UserDao extends MongoRepository<User, String> {
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

    @Query(value = "{ 'email' : :#{#email}, 'password' : :#{#password} }", fields = "{ 'role' : 1, '_id' : 0}")
    User findRoleByEmailAndPassword(@Param("email") String email, @Param("password") String password);

    default User findRoleByEmailAndPasswordWithException(String email, String password) throws DaoException{
        User user = findRoleByEmailAndPassword(email, password);
        if (user == null) {
            throw new DaoException(DaoTypeErrorsEnum.NOT_FOUND);
        }
        return user;
    }

    @Query(value = "{'email' : ?0}", fields = "{'configurations' : 1}")
    User findConfigurationsByEmail(String email);

    default List<String> findListOfConfigurationsByEmail(String email){
        User user = findConfigurationsByEmail(email);
        return user != null ? user.getConfigurations() : Collections.emptyList();
    }

    @Query(value = "{ 'email' : ?0, 'experiments.id' : ?1 }", exists = true)
    boolean existsUserByEmailAndExperimentId(String email, String experimentId);

}