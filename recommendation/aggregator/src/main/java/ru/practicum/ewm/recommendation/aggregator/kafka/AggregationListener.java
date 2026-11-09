package ru.practicum.ewm.recommendation.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.recommendation.aggregator.service.SimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationListener {

    private final SimilarityService similarityService;
    private final KafkaTemplate<Long, SpecificRecordBase> kafkaTemplate;

    @Value("${kafka.topics.events-similarity:stats.events-similarity.v1}")
    private String similarityTopic;

    @KafkaListener(topics = "${kafka.topics.user-actions:stats.user-actions.v1}")
    public void handleUserAction(UserActionAvro action) {
        log.debug("Aggregator получил действие: userId={}, eventId={}, type={}",
                action.getUserId(), action.getEventId(), action.getActionType());

        List<EventSimilarityAvro> similarities = similarityService.updateState(action);

        for (EventSimilarityAvro similarity : similarities) {
            kafkaTemplate.send(similarityTopic, similarity.getEventA(), similarity);
            log.info("Отправлено сходство мероприятий: eventA={}, eventB={}, score={}",
                    similarity.getEventA(), similarity.getEventB(), similarity.getScore());
        }
    }
}