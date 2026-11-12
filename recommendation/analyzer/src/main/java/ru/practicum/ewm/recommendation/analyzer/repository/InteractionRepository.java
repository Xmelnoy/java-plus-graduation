package ru.practicum.ewm.recommendation.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.recommendation.analyzer.model.Interaction;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    Optional<Interaction> findByUserIdAndEventId(Long userId, Long eventId);
    List<Interaction> findAllByUserId(Long userId);
    List<Interaction> findAllByEventIdIn(Collection<Long> eventIds);
}