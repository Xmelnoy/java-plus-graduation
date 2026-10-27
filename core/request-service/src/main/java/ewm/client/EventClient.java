package ewm.client;

import dto.EventShortDto;
import ewm.client.fallback.EventClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "events-service", path = "/events", fallback = EventClientFallback.class)
public interface EventClient {

    @GetMapping("/{id}")
    EventShortDto getEventById(@PathVariable("id") Long id);
}