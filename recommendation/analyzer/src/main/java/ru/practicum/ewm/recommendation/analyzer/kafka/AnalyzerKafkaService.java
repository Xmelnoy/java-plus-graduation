package ru.practicum.ewm.recommendation.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.recommendation.analyzer.model.EventSimilarity;
import ru.practicum.ewm.recommendation.analyzer.model.Interaction;
import ru.practicum.ewm.recommendation.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.recommendation.analyzer.repository.InteractionRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalyzerKafkaService {

    private final InteractionRepository interactionRepository;
    private final EventSimilarityRepository similarityRepository;

    @Transactional
    @KafkaListener(topics = "${kafka.topics.user-actions:stats.user-actions.v1}", groupId = "analyzer-actions-group")
    public void handleUserAction(UserActionAvro avro) {
        double weight = switch (avro.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };

        Optional<Interaction> existingOpt = interactionRepository.findByUserIdAndEventId(
                avro.getUserId(), avro.getEventId()
        );

        if (existingOpt.isPresent()) {
            Interaction existing = existingOpt.get();
            if (weight > existing.getRating()) {
                existing.setRating(weight);
                existing.setActionTs(avro.getTimestamp());
                interactionRepository.save(existing);
            }
        } else {
            interactionRepository.save(Interaction.builder()
                    .userId(avro.getUserId())
                    .eventId(avro.getEventId())
                    .rating(weight)
                    .actionTs(avro.getTimestamp())
                    .build());
        }
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topics.events-similarity:stats.events-similarity.v1}", groupId = "analyzer-similarity-group")
    public void handleEventSimilarity(EventSimilarityAvro avro) {
        EventSimilarity entity = similarityRepository
                .findByEventAAndEventB(avro.getEventA(), avro.getEventB())
                .orElseGet(() -> EventSimilarity.builder()
                        .eventA(avro.getEventA())
                        .eventB(avro.getEventB())
                        .build());

        entity.setScore(avro.getScore());
        entity.setSimilarityTs(avro.getTimestamp());
        similarityRepository.save(entity);
    }
}