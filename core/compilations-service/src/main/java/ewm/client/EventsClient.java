package ewm.client;

import dto.EventShortDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "events-service",
        path = "/internal/events"
)
public interface EventsClient {

    @GetMapping
    List<EventShortDto> getEventsByIds(
            @RequestParam("ids") List<Long> ids
    );
}
