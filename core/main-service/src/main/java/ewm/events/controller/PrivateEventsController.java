package ewm.events.controller;

import ewm.events.dto.EventFullDto;
import ewm.events.dto.EventShortDto;
import ewm.events.dto.NewEventDto;
import ewm.events.dto.UpdateEventUserRequest;
import ewm.events.dto.params.PaginationParams;
import ewm.events.dto.params.UserEventPathParams;
import ewm.events.service.EventsService;
import ewm.participationRequest.dto.EventRequestStatusUpdateRequest;
import ewm.participationRequest.dto.EventRequestStatusUpdateResult;
import ewm.participationRequest.dto.ParticipationRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateEventsController {

    private final EventsService eventsService;

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(@PathVariable Long userId,
                                                    @Valid @RequestBody NewEventDto dto) {

        log.info("POST /users/{}/events: creating event", userId);

        EventFullDto event = eventsService.createEvent(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            HttpServletRequest request) {

        log.info("GET /users/{}/events: from={}, size={}", userId, from, size);

        PaginationParams pagination = PaginationParams.builder()
                .from(from)
                .size(size)
                .build();

        List<EventShortDto> events = eventsService.getUserEvents(userId, pagination, request);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getUserEvent(@PathVariable Long userId, @PathVariable Long eventId) {

        log.info("GET /users/{}/events/{}", userId, eventId);

        UserEventPathParams pathParams = UserEventPathParams.builder()
                .userId(userId)
                .eventId(eventId)
                .build();

        EventFullDto event = eventsService.getUserEvent(pathParams);
        return ResponseEntity.ok(event);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @Valid @RequestBody UpdateEventUserRequest dto) {

        log.info("PATCH /users/{}/events/{}", userId, eventId);

        UserEventPathParams pathParams = UserEventPathParams.builder()
                .userId(userId)
                .eventId(eventId)
                .build();

        EventFullDto event = eventsService.updateUserEvent(pathParams, dto);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/{eventId}/requests")
    ResponseEntity<List<ParticipationRequestDto>> getEventRequests(@PathVariable long userId, @PathVariable long eventId) {
        log.info("GET /users/{}/events/{}/requests", userId, eventId);

        List<ParticipationRequestDto> eventRequests = eventsService.getEventRequests(userId, eventId);

        return ResponseEntity.ok(eventRequests);
    }

    @PatchMapping("/{eventId}/requests")
    ResponseEntity<EventRequestStatusUpdateResult> updateEventRequests(@PathVariable long userId,
                                                       @PathVariable long eventId,
                                                       @Valid @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("PATCH /users/{}/events/{}/requests", userId, eventId);

        EventRequestStatusUpdateResult result = eventsService.updateEventRequests(userId, eventId, request);

        return ResponseEntity.ok(result);
    }
}