package ru.practicum.ewm.stats.kafka.deserializer;

import ru.practicum.ewm.stats.avro.*;

public class UserActionDeserializer extends BaseAvroDeserializer<UserActionAvro> {
    public UserActionDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}
