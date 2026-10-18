package ewm.compilations.service;

import ewm.compilations.dto.NewCompilationDto;
import ewm.compilations.dto.UpdateCompilationRequest;
import ewm.compilations.mapper.CompilationMapper;
import ewm.compilations.model.Compilation;
import ewm.compilations.repository.CompilationRepository;
import ewm.events.model.Event;
import ewm.events.repository.EventsRepository;
import ewm.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ewm.compilations.dto.CompilationDto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventsRepository eventsRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public CompilationDto save(NewCompilationDto newCompilationDto) {
        log.info("save new compilation {}", newCompilationDto);
        Compilation compilation = compilationMapper.toCompilationFromNewDto(newCompilationDto);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            compilation.setEvents(
                    new HashSet<>(
                            eventsRepository.findAllById(newCompilationDto.getEvents())
                    )
            );
        }
        log.info("saves compilation {}", compilation);
        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.info("update compilation request {}", updateCompilationRequest);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Compilation with id=" + compId + " was not found"));

        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        if (updateCompilationRequest.getEvents() != null) {
            compilation.setEvents(
                    loadEvents(updateCompilationRequest.getEvents())
            );
        }
        log.info("Updates compilation {}", compilation);
        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public void delete(Long compId) {
        log.info("delete compilation {}", compId);
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException(
                    "Compilation with id=" + compId + " was not found");
        }

        compilationRepository.deleteById(compId);
        log.info("Deletes compilation {}", compId);
    }

    private Set<Event> loadEvents(List<Long> ids) {
        log.info("Load events {}", ids);
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        List<Event> events = eventsRepository.findAllById(ids);

        if (events.size() != ids.size()) {
            throw new NotFoundException("One or more events were not found");
        }
        log.info("Load events {}", events.size());
        return new HashSet<>(events);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("Get compilations pinned {} from {} size {}", pinned, from, size);
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable);
        log.info("get compilations {}", compilations.size());
        return compilations.stream()
                .map(compilationMapper::toCompilationDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("get compilation by id {}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        "Compilation with id=" + compId + " was not found"
                ));
        log.info("Get compilation by id {}", compilation);
        return compilationMapper.toCompilationDto(compilation);
    }
}
