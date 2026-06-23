package projekt.crossing.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import projekt.crossing.model.User;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
}