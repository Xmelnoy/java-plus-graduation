package ewm.client.fallback;

import dto.EventShortDto;
import ewm.client.EventClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventClientFallback implements EventClient {

    @Override
    public EventShortDto getEventById(Long id) {
        log.warn("events-service недоступен. Возвращаем заглушку для события id={}", id);
        EventShortDto dto = new EventShortDto();
        dto.setId(id);
        return dto;
    }
}