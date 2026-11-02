package ewm.participationRequest.controller;

import ewm.participationRequest.dto.EventRequestsCountDto;
import ewm.participationRequest.service.ParticipationRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class InternalRequestController {

    private final ParticipationRequestService requestService;

    @GetMapping("/confirmed-counts")
    public ResponseEntity<Map<Long, Long>> getConfirmedRequestsCount(
            @RequestParam("eventIds") List<Long> eventIds
    ) {
        List<EventRequestsCountDto> counts =
                requestService.getConfirmedRequestsCounts(eventIds);

        Map<Long, Long> result = counts.stream()
                .collect(Collectors.toMap(
                        EventRequestsCountDto::eventId,
                        EventRequestsCountDto::confirmedRequests
                ));

        return ResponseEntity.ok(result);
    }
}