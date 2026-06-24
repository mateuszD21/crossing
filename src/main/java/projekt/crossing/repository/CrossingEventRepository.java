package projekt.crossing.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import projekt.crossing.model.CrossingEvent;
import projekt.crossing.model.SystemState;

import java.util.List;

@Repository
public interface CrossingEventRepository extends MongoRepository<CrossingEvent, Long> {
    List<CrossingEvent> findAllByOrderByTimestampDesc();
}