package ewm.participationRequest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ewm.participationRequest.model.ParticipationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParticipationRequestDto {
    private Long id;
    private Long requester;
    private Long event;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    private ParticipationStatus status;
}
