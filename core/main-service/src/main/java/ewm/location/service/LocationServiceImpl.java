package ewm.location.service;

import ewm.events.dto.EventShortDto;
import ewm.events.mapper.EventMapper;
import ewm.events.repository.EventsRepository;
import ewm.exception.NotFoundException;
import ewm.exception.ValidationException;
import ewm.location.dto.DistanceDto;
import ewm.location.dto.EventDistanceDto;
import ewm.location.dto.LocationDto;
import ewm.location.dto.NewLocationDto;
import ewm.location.dto.UpdateLocationDto;
import ewm.location.mapper.LocationMapper;
import ewm.location.model.Location;
import ewm.location.model.LocationType;
import ewm.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final EventMapper eventMapper;
    private final EventsRepository eventRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional
    public LocationDto create(NewLocationDto dto) {
        validateLocationType(dto.getLocationType());

        Location location = locationMapper.toEntity(dto);
        Location saved = locationRepository.save(location);
        return locationMapper.toDto(saved);
    }

    @Override
    public Page<LocationDto> getAll(Pageable pageable) {
        return locationRepository.findAll(pageable).map(locationMapper::toDto);
    }

    @Override
    public LocationDto getById(Long id) {
        Location location = getLocationOrThrow(id);
        return locationMapper.toDto(location);
    }

    @Override
    @Transactional
    public LocationDto update(Long id, UpdateLocationDto dto) {
        validateUpdate(dto);

        Location existing = getLocationOrThrow(id);

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getLocationType() != null) {
            existing.setLocationType(LocationType.valueOf(dto.getLocationType().toUpperCase()));
        }
        if (dto.getLat() != null && dto.getLon() != null) {
            existing.setLat(dto.getLat());
            existing.setLon(dto.getLon());
            existing.setCenter(createPoint(dto.getLon(), dto.getLat()));
        }
        if (dto.getRadiusMeters() != null) {
            existing.setRadiusMeters(dto.getRadiusMeters());
        }

        Location updated = locationRepository.save(existing);
        return locationMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new NotFoundException("Локация не найдена с id: " + id);
        }
        locationRepository.deleteById(id);
    }

    @Override
    public List<LocationDto> findLocationsContainingPoint(Float lat, Float lon) {
        Point point = createPoint(lon, lat);
        return locationRepository.findLocationsContainingPoint(point)
                .stream()
                .map(locationMapper::toDto)
                .toList();
    }

    @Override
    public List<EventShortDto> findEventsInLocation(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new NotFoundException("Локация не найдена с id: " + locationId);
        }
        return locationRepository.findEventsInLocation(locationId)
                .stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public List<EventDistanceDto> findEventsWithinRadius(Float lat, Float lon, Double radius) {
        if (lat == null || lon == null) {
            throw new ValidationException("Координаты lat и lon обязательны");
        }
        if (radius == null || radius <= 0) {
            throw new ValidationException("Радиус должен быть положительным числом");
        }
        if (lat < -90.0 || lat > 90.0) {
            throw new ValidationException("Широта должна быть от -90 до 90");
        }
        if (lon < -180.0 || lon > 180.0) {
            throw new ValidationException("Долгота должна быть от -180 до 180");
        }

        List<Object[]> results = locationRepository.findEventsWithDistanceWithinRadius(lat, lon, radius);
        return results.stream()
                .map(this::mapToEventDistanceDto)
                .toList();
    }

    @Override
    public DistanceDto getDistanceToEvent(Long eventId, Float lat, Float lon) {
        if (eventId == null) {
            throw new ValidationException("ID события обязателен");
        }
        if (lat == null || lon == null) {
            throw new ValidationException("Координаты lat и lon обязательны");
        }
        if (lat < -90.0 || lat > 90.0) {
            throw new ValidationException("Широта должна быть от -90 до 90");
        }
        if (lon < -180.0 || lon > 180.0) {
            throw new ValidationException("Долгота должна быть от -180 до 180");
        }

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        Double distance = locationRepository.findDistanceToEvent(eventId, lat, lon);
        if (distance == null) {
            throw new NotFoundException("Не удалось вычислить расстояние до события с id=" + eventId);
        }

        return DistanceDto.builder()
                .eventId(eventId)
                .distance(distance)
                .build();
    }

    private Location getLocationOrThrow(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Локация не найдена с id: " + id));
    }

    private Point createPoint(double lon, double lat) {
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
        point.setSRID(4326);
        return point;
    }

    private EventDistanceDto mapToEventDistanceDto(Object[] row) {
        return EventDistanceDto.builder()
                .eventId(((Number) row[0]).longValue())
                .title((String) row[1])
                .annotation((String) row[2])
                .eventDate(((Timestamp) row[3]).toLocalDateTime())
                .paid((Boolean) row[4])
                .distance(((Number) row[5]).doubleValue())
                .build();
    }

    private void validateLocationType(String locationType) {
        if (locationType == null || locationType.isBlank()) {
            throw new ValidationException("Тип локации обязателен");
        }
        try {
            LocationType.valueOf(locationType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    "Недопустимое значение locationType: '" + locationType +
                            "'. Допустимые значения: " + Arrays.toString(LocationType.values())
            );
        }
    }

    public void validateUpdate(UpdateLocationDto dto) {
        if (dto.getName() != null) {
            if (dto.getName().isBlank()) {
                throw new ValidationException("Название локации не может быть пустым");
            }
            if (dto.getName().length() > 255) {
                throw new ValidationException("Название локации не должно превышать 255 символов");
            }
        }

        if (dto.getDescription() != null && dto.getDescription().length() > 1000) {
            throw new ValidationException("Описание локации не должно превышать 1000 символов");
        }

        if (dto.getLocationType() != null) {
            validateLocationType(dto.getLocationType());
        }

        if (dto.getLat() != null || dto.getLon() != null) {
            if (dto.getLat() == null || dto.getLon() == null) {
                throw new ValidationException("Координаты lat и lon должны быть указаны вместе");
            }
            if (dto.getLat() < -90.0 || dto.getLat() > 90.0) {
                throw new ValidationException("Широта должна быть от -90 до 90");
            }
            if (dto.getLon() < -180.0 || dto.getLon() > 180.0) {
                throw new ValidationException("Долгота должна быть от -180 до 180");
            }
        }

        if (dto.getRadiusMeters() != null && dto.getRadiusMeters() <= 0) {
            throw new ValidationException("Радиус должен быть положительным числом");
        }
    }
}