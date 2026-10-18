package ewm.location.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistanceDto {
    private Long eventId;
    private Double distance;
}