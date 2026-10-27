package ewm.client;

import dto.LocationDto;
import ewm.client.fallback.LocationClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "location-service", path = "/locations", fallback = LocationClientFallback.class)
public interface LocationClient {

    @GetMapping("/{id}")
    LocationDto getLocationById(@PathVariable("id") Long id);
}