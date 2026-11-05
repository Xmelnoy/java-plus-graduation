package ewm.client;

import dto.LocationCoordinatesDto;
import dto.LocationDto;
import ewm.client.fallback.LocationClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "location-service", path = "/internal/locations", fallback = LocationClientFallback.class)
public interface LocationClient {

    @GetMapping("/{id}")
    LocationDto getLocationById(@PathVariable("id") Long id);

    @PostMapping
    LocationDto createLocation(
            @RequestBody LocationCoordinatesDto dto
    );
}