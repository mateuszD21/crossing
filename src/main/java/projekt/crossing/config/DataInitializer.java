package projekt.crossing.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import projekt.crossing.model.CrossingEvent;
import projekt.crossing.model.User;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    public DataInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        if (!mongoTemplate.collectionExists(User.class)) {
            mongoTemplate.createCollection(User.class);
            System.out.println("Utworzono kolekcję users.");
        }
        if (!mongoTemplate.collectionExists(CrossingEvent.class)) {
            mongoTemplate.createCollection(CrossingEvent.class);
            System.out.println("Utworzono kolekcję crossing_events.");
        }
    }
}