package ewm.location.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDto {
    private Long id;
    private Float lat;
    private Float lon;
    private String name;
    private String description;
    private String locationType;
    private Double radiusMeters;
}
