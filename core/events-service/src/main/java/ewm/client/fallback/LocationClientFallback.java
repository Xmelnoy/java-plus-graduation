package ewm.client.fallback;

import dto.LocationCoordinatesDto;
import dto.LocationDto;
import ewm.client.LocationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocationClientFallback implements LocationClient {

    @Override
    public LocationDto getLocationById(Long id) {
        log.warn("location-service недоступен. Возвращаем заглушку для локации id={}", id);
        LocationDto fallback = new LocationDto();
        fallback.setId(id);
        fallback.setLat(0.0F);
        fallback.setLon(0.0F);

        return fallback;
    }

    @Override
    public LocationDto createLocation(LocationCoordinatesDto dto) {
        log.error(
                "location-service недоступен. Не удалось создать локацию: lat={}, lon={}",
                dto.getLat(),
                dto.getLon()
        );

        throw new IllegalStateException(
                "Не удалось создать локацию: location-service недоступен"
        );
    }
}