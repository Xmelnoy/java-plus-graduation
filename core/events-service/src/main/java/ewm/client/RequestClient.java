package ewm.client;

import ewm.client.fallback.RequestClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", path = "/requests", fallback = RequestClientFallback.class)
public interface RequestClient {

    @GetMapping("/confirmed-counts")
    Map<Long, Long> getConfirmedRequestsCount(
            @RequestParam("eventIds") List<Long> eventIds,
            @RequestParam(value = "userId", required = false) Long userId
    );
}