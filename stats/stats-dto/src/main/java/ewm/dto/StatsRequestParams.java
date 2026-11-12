package ewm.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Параметры запроса статистики.
 */
@Data
public final class StatsRequestParams {

    /** Начало временного диапазона. */
    @NotNull(message = "Дата начала обязательна")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;

    /** Конец временного диапазона. */
    @NotNull(message = "Дата конца обязательна")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;

    /** Список URI для фильтрации статистики. */
    private List<String> uris;

    /** Признак учета уникальных IP-адресов. */
    private Boolean unique = false;

    /**
     * Проверяет корректность временного диапазона.
     *
     * @return true, если дата начала не позже даты окончания
     */
    @AssertTrue(message = "Дата начала не может быть позже даты окончания")
    public boolean isDateRangeValid() {
        if (start == null || end == null) {
            return true;
        }
        return !start.isAfter(end);
    }
}