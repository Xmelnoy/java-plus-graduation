package ewm.location.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ewm.events.dto.EventShortDto;
import ewm.location.dto.LocationDto;
import ewm.location.dto.NewLocationDto;
import ewm.location.dto.UpdateLocationDto;
import ewm.location.service.LocationService;

import java.util.List;

@RestController
@RequestMapping("/admin/locations")
@RequiredArgsConstructor
public class AdminLocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationDto> create(@Valid @RequestBody NewLocationDto dto) {
        LocationDto created = locationService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping
    public ResponseEntity<Page<LocationDto>> getAll(@PageableDefault Pageable pageable) {
        Page<LocationDto> locations = locationService.getAll(pageable);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<LocationDto> getById(@PathVariable Long locationId) {
        LocationDto location = locationService.getById(locationId);
        return ResponseEntity.ok(location);
    }

    @PatchMapping("/{locationId}")
    public ResponseEntity<LocationDto> update(
            @PathVariable Long locationId,
            @RequestBody UpdateLocationDto dto) {
        LocationDto updated = locationService.update(locationId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> delete(@PathVariable Long locationId) {
        locationService.delete(locationId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<LocationDto>> search(
            @RequestParam Float lat,
            @RequestParam Float lon) {
        List<LocationDto> locations = locationService.findLocationsContainingPoint(lat, lon);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{locationId}/events")
    public ResponseEntity<List<EventShortDto>> getEventsInLocation(@PathVariable Long locationId) {
        List<EventShortDto> events = locationService.findEventsInLocation(locationId);
        return ResponseEntity.ok(events);
    }
}