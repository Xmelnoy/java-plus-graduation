package ewm.location.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLocationDto {

    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String name;

    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    private String locationType;

    @DecimalMin(value = "-90.0", message = "Широта должна быть от -90 до 90")
    @DecimalMax(value = "90.0", message = "Широта должна быть от -90 до 90")
    private Float lat;

    @DecimalMin(value = "-180.0", message = "Долгота должна быть от -180 до 180")
    @DecimalMax(value = "180.0", message = "Долгота должна быть от -180 до 180")
    private Float lon;

    @Positive(message = "Радиус должен быть положительным числом")
    private Double radiusMeters;
}