package ewm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO с результатами просмотра статистики.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatsDto {

    /** Название сервиса. */
    private String app;

    /** URI запрашиваемого ресурса. */
    private String uri;

    /** Количество обращений к ресурсу. */
    private Long hits;
}