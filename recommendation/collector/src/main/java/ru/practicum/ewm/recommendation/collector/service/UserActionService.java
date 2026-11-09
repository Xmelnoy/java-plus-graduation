package ru.practicum.ewm.recommendation.collector.service;

import ru.practicum.ewm.stats.proto.UserActionProto;

public interface UserActionService {

    void collectUserAction(UserActionProto proto);
}
