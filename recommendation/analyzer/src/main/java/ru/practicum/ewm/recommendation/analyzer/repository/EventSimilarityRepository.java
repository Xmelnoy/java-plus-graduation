package ru.practicum.ewm.recommendation.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.recommendation.analyzer.model.EventSimilarity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {
    Optional<EventSimilarity> findByEventAAndEventB(Long eventA, Long eventB);

    @Query("SELECT s FROM EventSimilarity s WHERE s.eventA = :eventId OR s.eventB = :eventId")
    List<EventSimilarity> findAllByEvent(@Param("eventId") Long eventId);

    @Query("SELECT s FROM EventSimilarity s WHERE s.eventA IN :eventIds OR s.eventB IN :eventIds")
    List<EventSimilarity> findAllByEventIn(@Param("eventIds") Collection<Long> eventIds);
}