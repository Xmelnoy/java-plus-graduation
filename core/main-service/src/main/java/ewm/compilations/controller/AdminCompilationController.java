package ewm.compilations.controller;

import ewm.compilations.dto.NewCompilationDto;
import ewm.compilations.dto.CompilationDto;
import ewm.compilations.dto.UpdateCompilationRequest;
import ewm.compilations.service.CompilationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    public ResponseEntity<CompilationDto> save(@Valid @RequestBody NewCompilationDto dto) {
        log.info("POST /admin/compilations: NewCompilationDto={}", dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(compilationService.save(dto));
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> update(@PathVariable Long compId,
                                                 @Valid @RequestBody UpdateCompilationRequest dto) {
        log.info("PATCH /admin/compilations/{}: UpdateCompilationRequest={}", compId, dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(compilationService.update(compId, dto));
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<Void> delete(@PathVariable Long compId) {
        log.info("DELETE /admin/compilations/{}: delete", compId);
        compilationService.delete(compId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
