package ru.practicum.ewm.recommendation.aggregator.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

public interface SimilarityService {
    List<EventSimilarityAvro> updateState(UserActionAvro action);
}