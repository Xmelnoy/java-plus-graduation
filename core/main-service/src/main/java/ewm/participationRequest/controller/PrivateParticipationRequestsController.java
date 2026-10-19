package ewm.participationRequest.controller;

import ewm.participationRequest.dto.ParticipationRequestDto;
import ewm.participationRequest.service.ParticipationRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateParticipationRequestsController {
    private final ParticipationRequestService requestService;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable Long userId) {
        log.info("GET /users/{userId}/requests: getUserRequests with user_id={}", userId);

        List<ParticipationRequestDto> requests = requestService.getUserRequests(userId);

        return ResponseEntity.ok(requests);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ParticipationRequestDto> addParticipationRequest(
            @PathVariable Long userId, @RequestParam Long eventId) {
        log.info("POST /users/{userId}/requests: addParticipationRequest with user_id={} and event_id={}",
            userId, eventId);

        ParticipationRequestDto saveRequest = requestService.addParticipationRequest(userId, eventId);

        return ResponseEntity.status(HttpStatus.CREATED).body(saveRequest);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("PATCH /users/{userId}/requests/{requestId}/cancel: cancelRequest with user_id={} and request_id={}",
            userId, requestId);

        ParticipationRequestDto cancelRequest = requestService.cancelRequest(userId, requestId);

        return ResponseEntity.ok(cancelRequest);
    }
}
