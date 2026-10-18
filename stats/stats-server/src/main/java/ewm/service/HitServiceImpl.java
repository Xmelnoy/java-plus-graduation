package ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ewm.dto.EndpointHitDto;
import ewm.dto.ViewStatsDto;
import ewm.mapper.HitMapper;
import ewm.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;
    private final HitMapper hitMapper;

    @Override
    @Transactional
    public void save(EndpointHitDto hitDto) {
        hitRepository.save(hitMapper.toEntity(hitDto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       Boolean unique) {
        List<String> urisForQuery = (uris == null || uris.isEmpty()) ? null : uris;

        return Boolean.TRUE.equals(unique)
                ? hitRepository.getStatsUnique(start, end, urisForQuery)
                : hitRepository.getStats(start, end, urisForQuery);
    }
}
