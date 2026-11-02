package ewm.location.controller;

import dto.LocationCoordinatesDto;
import dto.LocationDto;
import ewm.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/locations")
@RequiredArgsConstructor
public class InternalLocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationDto> create(
            @RequestBody LocationCoordinatesDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationService.createFromCoordinates(
                        dto.getLat(),
                        dto.getLon()
                ));
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<LocationDto> getById(
            @PathVariable Long locationId
    ) {
        return ResponseEntity.ok(
                locationService.getById(locationId)
        );
    }
}