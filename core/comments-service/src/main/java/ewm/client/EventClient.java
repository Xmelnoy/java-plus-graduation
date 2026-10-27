package ewm.client;

import dto.EventShortDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service", path = "/events")
public interface EventClient {

    @GetMapping("/{id}")
    EventShortDto getEventById(@PathVariable("id") Long id);
}
