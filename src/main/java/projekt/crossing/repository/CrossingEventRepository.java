package projekt.crossing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projekt.crossing.model.CrossingEvent;
import projekt.crossing.model.SystemState;

import java.util.List;

@Repository
public interface CrossingEventRepository extends JpaRepository<CrossingEvent, Long> {
    List<CrossingEvent> findAllByOrderByTimestampDesc();
    List<CrossingEvent> findByToState(SystemState state);
}