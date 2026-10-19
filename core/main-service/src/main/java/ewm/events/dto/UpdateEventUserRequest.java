package ewm.events.dto;

import ewm.location.dto.LocationDto;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000, message = "Аннотация должна быть от 20 до 2000 символов")
    private String annotation;
    private Long category;

    @Size(min = 20, max = 7000, message = "Описание должно быть от 20 до 7000 символов")
    private String description;
    private String eventDate;
    private LocationDto location;
    private Boolean paid;

    @PositiveOrZero(message = "Количество участников не может быть меньше нуля")
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateAction stateAction;

    @Size(min = 3, max = 120, message = "Заголовок должен быть от 3 до 120 символов")
    private String title;
}