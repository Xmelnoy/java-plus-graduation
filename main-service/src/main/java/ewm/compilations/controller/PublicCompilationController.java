package ewm.compilations.controller;

import ewm.compilations.dto.CompilationDto;
import ewm.compilations.service.CompilationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Slf4j
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping
    public ResponseEntity<List<CompilationDto>> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("GET /compilations pinned={} from={} size={}", pinned, from, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(compilationService.getCompilations(pinned, from, size));
    }

    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDto> getCompilation(@PathVariable Long compId) {
        log.info("GET /compilations/{}", compId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(compilationService.getCompilationById(compId));
    }
}
