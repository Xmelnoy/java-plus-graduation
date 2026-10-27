package ewm.participationRequest.dto;

public record EventRequestsCountDto(
        Long eventId,
        Long confirmedRequests
) {
}
