package ru.practicum.ewm.recommendation.aggregator.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimilarityServiceImpl implements SimilarityService {

    private final Map<Long, Map<Long, Double>> userEventWeights = new HashMap<>();

    private final Map<Long, Double> eventWeightSums = new HashMap<>();

    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    @Override
    public List<EventSimilarityAvro> updateState(UserActionAvro action) {
        long userId = action.getUserId();
        long eventA = action.getEventId();
        double newWeight = toWeight(action.getActionType());
        Instant timestamp = action.getTimestamp();

        Map<Long, Double> userWeightsForEventA = userEventWeights.computeIfAbsent(eventA, k -> new HashMap<>());
        double oldWeight = userWeightsForEventA.getOrDefault(userId, 0.0);

        if (newWeight <= oldWeight) {
            return List.of();
        }

        userWeightsForEventA.put(userId, newWeight);
        double deltaA = newWeight - oldWeight;
        double sumA = eventWeightSums.merge(eventA, deltaA, Double::sum);

        List<EventSimilarityAvro> result = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : userEventWeights.entrySet()) {
            long eventB = entry.getKey();
            if (eventA == eventB) {
                continue;
            }

            Double weightB = entry.getValue().get(userId);
            if (weightB == null || weightB == 0.0) {
                continue;
            }

            double oldMin = Math.min(oldWeight, weightB);
            double newMin = Math.min(newWeight, weightB);
            double deltaMin = newMin - oldMin;

            double currentMinSum = getMinWeightSum(eventA, eventB) + deltaMin;
            putMinWeightSum(eventA, eventB, currentMinSum);

            double sumB = eventWeightSums.getOrDefault(eventB, 0.0);
            double denominator = Math.sqrt(sumA) * Math.sqrt(sumB);
            double score = (denominator == 0.0) ? 0.0 : currentMinSum / denominator;

            long first = Math.min(eventA, eventB);
            long second = Math.max(eventA, eventB);

            result.add(EventSimilarityAvro.newBuilder()
                    .setEventA(first)
                    .setEventB(second)
                    .setScore(score)
                    .setTimestamp(timestamp)
                    .build());
        }

        return result;
    }

    private void putMinWeightSum(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        minWeightsSums.computeIfAbsent(first, e -> new HashMap<>()).put(second, sum);
    }

    private double getMinWeightSum(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        return minWeightsSums.computeIfAbsent(first, e -> new HashMap<>()).getOrDefault(second, 0.0);
    }

    private double toWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}