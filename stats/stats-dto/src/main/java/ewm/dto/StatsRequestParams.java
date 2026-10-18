package ewm.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StatsRequestParams {

    @NotNull(message = "Дата начала обязательна")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;

    @NotNull(message = "Дата конца обязательна")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;

    private List<String> uris;

    private Boolean unique = false;

    @AssertTrue(message = "Дата начала не может быть позже даты окончания")
    public boolean isDateRangeValid() {
        if (start == null || end == null) {
            return true;
        }
        return !start.isAfter(end);
    }
}