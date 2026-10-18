package ewm.controller;

import ewm.dto.EndpointHitDto;
import ewm.dto.StatsRequestParams;
import ewm.dto.ViewStatsDto;
import ewm.service.HitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final HitService hitService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> hit(@Valid @RequestBody EndpointHitDto hitDto) {
        log.info("POST /hit: app={}, uri={}, ip={}", hitDto.getApp(), hitDto.getUri(), hitDto.getIp());
        hitService.save(hitDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(@Valid StatsRequestParams params) {
        log.info("GET /stats: start={}, end={}, uris={}, unique={}",
                params.getStart(), params.getEnd(), params.getUris(), params.getUnique());



        return ResponseEntity.ok(hitService.getStats(
                params.getStart(),
                params.getEnd(),
                params.getUris(),
                params.getUnique()
        ));
    }
}