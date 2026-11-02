package ewm.compilations.service;

import ewm.client.EventsClient;
import ewm.compilations.dto.NewCompilationDto;
import ewm.compilations.dto.UpdateCompilationRequest;
import ewm.compilations.mapper.CompilationMapper;
import ewm.compilations.model.Compilation;
import ewm.compilations.repository.CompilationRepository;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ewm.compilations.dto.CompilationDto;
import dto.EventShortDto;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventsClient eventsClient;
    private final CompilationMapper compilationMapper;

    @Override
    public CompilationDto save(NewCompilationDto newCompilationDto) {
        log.info("save new compilation {}", newCompilationDto);
        Compilation compilation = compilationMapper.toCompilationFromNewDto(newCompilationDto);

        List<Long> eventIds = newCompilationDto.getEvents();

        List<EventShortDto> events = loadEvents(eventIds);

        compilation.setEventIds(eventIds == null
                ? new HashSet<>()
                : new HashSet<>(eventIds));

        log.info("saves compilation {}", compilation);

        Compilation saved = compilationRepository.save(compilation);

        CompilationDto result = compilationMapper.toCompilationDto(saved);
        result.setEvents(events);

        return result;
    }

    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        List<EventShortDto> events;

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
            List<Long> eventIds = updateCompilationRequest.getEvents();

            events = loadEvents(eventIds);

            compilation.setEventIds(new HashSet<>(eventIds));
        } else {
            events = loadEvents(
                    new ArrayList<>(compilation.getEventIds())
            );
        }
        log.info("Updates compilation {}", compilation);

        Compilation saved = compilationRepository.save(compilation);

        CompilationDto result = compilationMapper.toCompilationDto(saved);
        result.setEvents(events);

        return result;
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

    private List<EventShortDto> loadEvents(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        log.info("Load events {}", ids);

        List<Long> uniqueIds = ids.stream()
                .distinct()
                .toList();

        List<EventShortDto> events =
                eventsClient.getEventsByIds(uniqueIds);

        if (events == null) {
            throw new NotFoundException("Не удалось получить события");
        }

        Map<Long, EventShortDto> eventsById = events.stream()
                .collect(Collectors.toMap(
                        EventShortDto::getId,
                        event -> event,
                        (first, second) -> first
                ));

        if (!eventsById.keySet().containsAll(uniqueIds)) {
            throw new NotFoundException(
                    "Одно или несколько событий не найдены"
            );
        }

        return uniqueIds.stream()
                .map(eventsById::get)
                .toList();
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("Get compilations pinned {} from {} size {}", pinned, from, size);
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable);
        log.info("get compilations {}", compilations.size());
        return compilations.stream()
                .map(compilation -> {
                    CompilationDto dto =
                            compilationMapper.toCompilationDto(compilation);

                    dto.setEvents(loadEvents(
                            new ArrayList<>(compilation.getEventIds())
                    ));

                    return dto;
                })
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

        CompilationDto dto =
                compilationMapper.toCompilationDto(compilation);

        dto.setEvents(loadEvents(
                new ArrayList<>(compilation.getEventIds())
        ));

        return dto;
    }
}
