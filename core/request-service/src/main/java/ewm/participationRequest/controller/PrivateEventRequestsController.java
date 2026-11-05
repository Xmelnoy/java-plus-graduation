package ewm.participationRequest.controller;

import ewm.participationRequest.dto.EventRequestStatusUpdateRequest;
import ewm.participationRequest.dto.EventRequestStatusUpdateResult;
import ewm.participationRequest.dto.ParticipationRequestDto;
import ewm.participationRequest.service.ParticipationRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class PrivateEventRequestsController {

    private final ParticipationRequestService requestService;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getEventRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        return ResponseEntity.ok(
                requestService.getEventRequests(userId, eventId)
        );
    }

    @PatchMapping
    public ResponseEntity<EventRequestStatusUpdateResult> updateEventRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest request) {

        return ResponseEntity.ok(
                requestService.updateEventRequests(userId, eventId, request)
        );
    }
}