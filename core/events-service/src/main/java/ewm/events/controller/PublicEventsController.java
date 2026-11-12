package ewm.events.controller;

import client.AnalyzerClient; // Убедитесь, что импорты клиентов соответствуют вашему пакету
import client.CollectorClient;
import dto.EventShortDto;
import ewm.events.dto.EventFullDto;
import ewm.events.dto.EventSort;
import ewm.events.dto.params.PublicEventParams;
import ewm.events.service.EventsService;
import ewm.events.dto.DistanceDto;
import ewm.events.dto.EventDistanceDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.stats.proto.ActionTypeProto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/events")
public class PublicEventsController {

    private final EventsService eventsService;
    private final CollectorClient collectorClient;
    private final AnalyzerClient analyzerClient;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) EventSort sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            HttpServletRequest request
    ) {
        log.info("GET /events: text={}, categories={}, paid={}", text, categories, paid);

        PublicEventParams searchParams = PublicEventParams.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();

        return ResponseEntity.ok(eventsService.getEvents(searchParams, request));
    }

    @GetMapping("/locations/near")
    public ResponseEntity<List<EventDistanceDto>> findEventsWithinRadius(
            @RequestParam Float lat,
            @RequestParam Float lon,
            @RequestParam Double radius) {
        log.info("GET /events/locations/near: lat={}, lon={}, radius={}", lat, lon, radius);
        List<EventDistanceDto> events = eventsService.findEventsWithinRadius(lat, lon, radius);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}/locations/distance")
    public ResponseEntity<DistanceDto> getDistanceToEvent(
            @PathVariable Long eventId,
            @RequestParam Float lat,
            @RequestParam Float lon) {
        log.info("GET /events/{}/locations/distance: lat={}, lon={}", eventId, lat, lon);
        DistanceDto distance = eventsService.getDistanceToEvent(eventId, lat, lon);
        return ResponseEntity.ok(distance);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEvent(
            @PathVariable Long eventId,
            @RequestHeader(value = "X-EWM-USER-ID", required = false) Long userId,
            HttpServletRequest request
    ) {
        log.info("GET /events/{}", eventId);

        if (userId != null) {
            collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
        }

        return ResponseEntity.ok(
                eventsService.getEvent(eventId, request)
        );
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<EventShortDto>> getRecommendations(
            @RequestHeader("X-EWM-USER-ID") long userId,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        log.info("GET /events/recommendations for userId={}, size={}", userId, size);
        List<EventShortDto> recommendations = eventsService.getRecommendations(userId, size);
        return ResponseEntity.ok(recommendations);
    }

    @PutMapping("/{eventId}/like")
    public ResponseEntity<Void> likeEvent(
            @RequestHeader("X-EWM-USER-ID") long userId,
            @PathVariable Long eventId
    ) {
        log.info("PUT /events/{}/like by userId={}", eventId, userId);
        eventsService.likeEvent(userId, eventId);
        collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}