package ewm.client.fallback;

import ewm.client.RequestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RequestClientFallback implements RequestClient {

    @Override
    public Map<Long, Long> getConfirmedRequestsCount(List<Long> eventIds, Long userId) {
        log.warn("request-service недоступен. Возвращаем 0 заявок для событий {}", eventIds);
        return eventIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0L));
    }
}