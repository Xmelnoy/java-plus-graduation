package ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service", path = "/internal/events")
public interface EventClient {

    @GetMapping("/categories/{categoryId}/exists")
    boolean hasEventsByCategory(
            @PathVariable("categoryId") Long categoryId
    );
}