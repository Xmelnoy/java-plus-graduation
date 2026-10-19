package ewm.location.service;

import ewm.location.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ewm.events.dto.EventShortDto;

import java.util.List;

public interface LocationService {

    LocationDto create(NewLocationDto dto);

    Page<LocationDto> getAll(Pageable pageable);

    LocationDto getById(Long id);

    LocationDto update(Long id, UpdateLocationDto dto);

    void delete(Long id);

    List<LocationDto> findLocationsContainingPoint(Float lat, Float lon);

    List<EventShortDto> findEventsInLocation(Long locationId);

    List<EventDistanceDto> findEventsWithinRadius(Float lat, Float lon, Double radius);

    DistanceDto getDistanceToEvent(Long eventId, Float lat, Float lon);
}