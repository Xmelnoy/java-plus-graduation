package ru.practicum.ewm.recommendation.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.recommendation.analyzer.model.EventSimilarity;
import ru.practicum.ewm.recommendation.analyzer.model.Interaction;
import ru.practicum.ewm.recommendation.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.recommendation.analyzer.repository.InteractionRepository;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final InteractionRepository interactionRepository;
    private final EventSimilarityRepository similarityRepository;

    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        long eventId = request.getEventId();
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();

        Set<Long> userInteractedEvents = interactionRepository.findAllByUserId(userId).stream()
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());

        return similarityRepository.findAllByEvent(eventId).stream()
                .map(s -> {
                    long otherEventId = s.getEventA().equals(eventId) ? s.getEventB() : s.getEventA();
                    return Map.entry(otherEventId, s.getScore());
                })
                .filter(entry -> !userInteractedEvents.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .toList();
    }

    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();

        List<Interaction> userInteractions = interactionRepository.findAllByUserId(userId);
        if (userInteractions.isEmpty()) {
            return List.of();
        }

        Map<Long, Double> userRatings = userInteractions.stream()
                .collect(Collectors.toMap(Interaction::getEventId, Interaction::getRating));
        Set<Long> interactedEventIds = userRatings.keySet();

        Set<Long> recentEventIds = userInteractions.stream()
                .sorted(Comparator.comparing(Interaction::getActionTs).reversed())
                .limit(maxResults)
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());

        List<EventSimilarity> candidateSimilarities = similarityRepository.findAllByEventIn(recentEventIds);

        Map<Long, Double> bestCandidateScores = new HashMap<>();
        for (EventSimilarity s : candidateSimilarities) {
            long candidateId = recentEventIds.contains(s.getEventA()) ? s.getEventB() : s.getEventA();
            if (!interactedEventIds.contains(candidateId)) {
                bestCandidateScores.merge(candidateId, s.getScore(), Math::max);
            }
        }

        List<Long> topCandidates = bestCandidateScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .toList();

        if (topCandidates.isEmpty()) {
            return List.of();
        }

        List<EventSimilarity> allCandidateSims = similarityRepository.findAllByEventIn(topCandidates);
        List<RecommendedEventProto> result = new ArrayList<>();

        for (Long candidateId : topCandidates) {
            List<EventSimilarity> neighbors = allCandidateSims.stream()
                    .filter(s -> s.getEventA().equals(candidateId) || s.getEventB().equals(candidateId))
                    .filter(s -> {
                        long otherId = s.getEventA().equals(candidateId) ? s.getEventB() : s.getEventA();
                        return interactedEventIds.contains(otherId);
                    })
                    .sorted(Comparator.comparing(EventSimilarity::getScore).reversed())
                    .limit(maxResults)
                    .toList();

            double weightedSum = 0.0;
            double similaritySum = 0.0;

            for (EventSimilarity neighbor : neighbors) {
                long neighborId = neighbor.getEventA().equals(candidateId)
                        ? neighbor.getEventB()
                        : neighbor.getEventA();
                double rating = userRatings.getOrDefault(neighborId, 0.0);
                weightedSum += neighbor.getScore() * rating;
                similaritySum += neighbor.getScore();
            }

            double predictedScore = similaritySum > 0 ? (weightedSum / similaritySum) : 0.0;
            result.add(RecommendedEventProto.newBuilder()
                    .setEventId(candidateId)
                    .setScore(predictedScore)
                    .build());
        }

        return result.stream()
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .toList();
    }

    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Long> eventIds = request.getEventIdList();
        if (eventIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Double> sums = interactionRepository.findAllByEventIdIn(eventIds).stream()
                .collect(Collectors.groupingBy(
                        Interaction::getEventId,
                        Collectors.summingDouble(Interaction::getRating)
                ));

        return eventIds.stream()
                .map(id -> RecommendedEventProto.newBuilder()
                        .setEventId(id)
                        .setScore(sums.getOrDefault(id, 0.0))
                        .build())
                .toList();
    }
}