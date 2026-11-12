package ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи информации о посещении эндпоинта.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHitDto {

    /** Сообщение об ошибке при пустом названии сервиса. */
    private static final String APP_BLANK_ERROR =
            "название сервиса не может быть пустым";

    /** Сообщение об ошибке при пустом URI. */
    private static final String URI_BLANK_ERROR =
            "uri сервиса не может быть пустым";

    /** Сообщение об ошибке при пустом IP-адресе. */
    private static final String IP_BLANK_ERROR =
            "ip пользователя не может быть пустым";

    /** Сообщение об ошибке при отсутствии даты. */
    private static final String TIMESTAMP_BLANK_ERROR =
            "дата не может быть пустой";

    /** Идентификатор записи. */
    private Long id;

    /** Название сервиса. */
    @NotBlank(message = APP_BLANK_ERROR)
    private String app;

    /** URI запрашиваемого ресурса. */
    @NotBlank(message = URI_BLANK_ERROR)
    private String uri;

    /** IP-адрес пользователя. */
    @NotBlank(message = IP_BLANK_ERROR)
    private String ip;

    /** Дата и время обращения к ресурсу. */
    @NotNull(message = TIMESTAMP_BLANK_ERROR)
    @PastOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
