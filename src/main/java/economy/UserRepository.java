package economy;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<UserData, String> {
    // Spring Data MongoDB automatically creates these methods:
    // - save(UserData user) - saves/updates user
    // - findById(String id) - gets user by ID
    // - existsById(String id) - checks if user exists
    // - deleteById(String id) - deletes user
    // - findAll() - gets all users
}