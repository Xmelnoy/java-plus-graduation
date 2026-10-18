package ewm.participationRequest.dto;

import ewm.participationRequest.model.ParticipationStatus;
import lombok.Data;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    List<Long> requestIds;
    ParticipationStatus status;
}
