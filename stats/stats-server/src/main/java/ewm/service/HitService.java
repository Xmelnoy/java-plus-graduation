package ewm.service;

import ewm.dto.EndpointHitDto;
import ewm.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface HitService {
    void save(EndpointHitDto hitDto);

    List<ViewStatsDto> getStats(LocalDateTime start,
                                LocalDateTime end,
                                List<String> uris,
                                Boolean unique);
}
