package ewm.location.service;

import dto.LocationDto;
import ewm.location.dto.NewLocationDto;
import ewm.location.dto.UpdateLocationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LocationService {

    LocationDto create(NewLocationDto dto);

    Page<LocationDto> getAll(Pageable pageable);

    LocationDto getById(Long id);

    LocationDto update(Long id, UpdateLocationDto dto);

    void delete(Long id);

    List<LocationDto> findLocationsContainingPoint(Float lat, Float lon);

}