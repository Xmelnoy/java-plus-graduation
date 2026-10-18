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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHitDto {
    private static final String APP_BLANK_ERROR = "название сервиса не может быть пустым";
    private static final String URI_BLANK_ERROR = "uri сервиса не может быть пустым";
    private static final String IP_BLANK_ERROR = "ip пользователя не может быть пустым";
    private static final String TIMESTAMP_BLANK_ERROR = "дата не может быть пустой";
    private Long id;

    @NotBlank(message = APP_BLANK_ERROR)
    private String app;

    @NotBlank(message = URI_BLANK_ERROR)
    private String uri;

    @NotBlank(message = IP_BLANK_ERROR)
    private String ip;

    @NotNull(message = TIMESTAMP_BLANK_ERROR)
    @PastOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}