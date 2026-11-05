package ewm.events.controller;

import dto.EventShortDto;
import ewm.events.service.EventsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalEventController {

    private final EventsService eventsService;

    @GetMapping("/{eventId}")
    public ResponseEntity<EventShortDto> getEventById(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                eventsService.getInternalEvent(eventId)
        );
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEventsByIds(
            @RequestParam("ids") List<Long> ids
    ) {
        return ResponseEntity.ok(
                eventsService.getInternalEvents(ids)
        );
    }

    @GetMapping("/categories/{categoryId}/exists")
    public ResponseEntity<Boolean> hasEventsByCategory(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(
                eventsService.hasEventsByCategory(categoryId)
        );
    }
}