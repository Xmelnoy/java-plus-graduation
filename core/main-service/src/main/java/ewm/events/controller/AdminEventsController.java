package ewm.events.controller;

import ewm.events.dto.EventFullDto;
import ewm.events.dto.UpdateEventAdminRequest;
import ewm.events.dto.params.AdminEventParams;
import ewm.events.service.EventsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminEventsController {

    private final EventsService eventsService;

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size
    ) {
        log.info("GET /admin/events: users={}, states={}, categories={}", users, states, categories);

        AdminEventParams params = AdminEventParams.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        List<EventFullDto> events = eventsService.getAdminEvents(params);
        return ResponseEntity.ok(events);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEventByAdmin(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest request
    ) {
        log.info("PATCH /admin/events/{}", eventId);

        EventFullDto event = eventsService.updateAdminEvent(eventId, request);
        return ResponseEntity.ok(event);
    }

}
