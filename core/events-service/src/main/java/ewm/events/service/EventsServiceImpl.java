package ewm.events.service;

import client.StatsClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.*;
import ewm.client.CategoryClient;
import ewm.client.LocationClient;
import ewm.client.RequestClient;
import ewm.client.UserClient;
import ewm.dto.EndpointHitDto;
import ewm.dto.ViewStatsDto;
import ewm.events.dto.*;
import ewm.events.dto.params.AdminEventParams;
import ewm.events.dto.params.PaginationParams;
import ewm.events.dto.params.PublicEventParams;
import ewm.events.dto.params.UserEventPathParams;
import ewm.events.mapper.EventMapper;
import ewm.events.model.Event;
import enums.State;
import ewm.events.repository.EventsRepository;
import ewm.events.repository.specification.EventSpecification;
import exception.ConflictException;
import exception.NotFoundException;
import exception.ValidationException;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventsServiceImpl implements EventsService {

    private final EventsRepository eventsRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    private final UserClient userClient;
    private final CategoryClient categoryClient;
    private final RequestClient requestClient;
    private final LocationClient locationClient;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final String APP_NAME = "ewm-main-service";
    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private double calculateDistance(
            double lat1, double lon1,
            double lat2, double lon2) {

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(latDistance / 2), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(lonDistance / 2), 2);

        return EARTH_RADIUS_METERS
                * 2
                * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(PublicEventParams params, HttpServletRequest request) {
        LocalDateTime startDate = params.getRangeStart();
        LocalDateTime endDate = params.getRangeEnd();

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ValidationException("rangeStart must be before rangeEnd");
        }

        sendHitToStats("/ewm/events", request.getRemoteAddr());

        Specification<Event> spec = Specification
                .where(EventSpecification.published())
                .and(EventSpecification.hasText(params.getText()))
                .and(EventSpecification.hasCategories(params.getCategories()))
                .and(EventSpecification.hasPaid(params.getPaid()))
                .and(EventSpecification.dateAfter(startDate))
                .and(EventSpecification.dateBefore(endDate));

        return switch (params.getSort()) {
            case VIEWS -> getEventsSortedByViews(spec, params);
            case EVENT_DATE -> getEventsSortedByDate(spec, params);
            case null -> getEventsSortedByDate(spec, params);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAdminEvents(AdminEventParams params) {
        LocalDateTime startDate = params.getRangeStart();
        LocalDateTime endDate = params.getRangeEnd();

        PageRequest pageRequest = PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                Sort.by(Sort.Direction.DESC, "eventDate")
        );

        List<State> states = params.getStates() == null
                ? null
                : params.getStates().stream()
                .map(State::valueOf)
                .toList();

        Specification<Event> spec = Specification
                .where(EventSpecification.hasUser(params.getUsers()))
                .and(EventSpecification.hasStates(states)
                        .and(EventSpecification.hasCategories(params.getCategories()))
                        .and(EventSpecification.dateAfter(startDate))
                        .and(EventSpecification.dateBefore(endDate)));

        Page<Event> page = eventsRepository.findAll(spec, pageRequest);
        List<Event> events = page.getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<EventFullDto> eventFullDtoList = eventMapper.toEventFullDtoList(events);
        enrichEventFullDtosWithStats(eventFullDtoList);

        return eventFullDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        Event event = eventsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event с id=" + id + " не найден"));

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException("Event с id=" + id + " не найден");
        }

        sendHitToStats("/ewm/events/" + id, request.getRemoteAddr());

        EventFullDto dto = eventMapper.toEventFullDto(event);
        enrichEventWithStats(dto);

        dto.setCategory(categoryClient.getCategoryById(event.getCategoryId()));
        dto.setInitiator(userClient.getUserById(event.getInitiatorId()));
        dto.setLocation(locationClient.getLocationById(event.getLocationId()));

        return dto;
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        UserShortDto user = getUserOrThrow(userId);
        CategoryDto category = getCategoryOrThrow(dto.getCategory());

        LocationDto location = locationClient.createLocation(
                new LocationCoordinatesDto(
                        dto.getLocation().getLat(),
                        dto.getLocation().getLon()
                )
        );

        if (location == null || location.getId() == null) {
            throw new IllegalStateException(
                    "Не удалось создать локацию для события"
            );
        }

        LocalDateTime eventDate = parseAndValidateEventDate(
                dto.getEventDate(),
                "Дата события должна быть не ранее чем через 2 часа от текущего момента"
        );

        Event event = eventMapper.toEvent(dto, category, user);

        event.setLocationId(location.getId());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);
        event.setEventDate(eventDate);

        event = eventsRepository.save(event);

        EventFullDto result = eventMapper.toEventFullDto(event);

        result.setCategory(category);
        result.setInitiator(user);
        result.setLocation(location);

        result.setViews(0L);
        result.setConfirmedRequests(0L);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getUserEvent(UserEventPathParams params) {
        Event event = eventsRepository.findByIdAndInitiatorId(params.getEventId(), params.getUserId())
                .orElseThrow(() -> new NotFoundException("Event с id=" + params.getEventId() + " не найден"));

        EventFullDto dto = eventMapper.toEventFullDto(event);
        enrichEventWithStats(dto);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, PaginationParams params, HttpServletRequest request) {
        getUserOrThrow(userId);

        sendHitToStats("/users/" + userId + "/ewm/events", request.getRemoteAddr());

        PageRequest pageRequest = PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                Sort.by(Sort.Direction.DESC, "eventDate"));
        Page<Event> page = eventsRepository.findByInitiatorId(userId, pageRequest);
        List<Event> events = page.getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<EventShortDto> dtos = eventMapper.toEventShortDtoList(events);
        enrichEventsWithStats(dtos);

        return dtos;
    }

    @Override
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event с id=" + eventId + " не найден"));

        updateAdminEventFields(event, request);
        handleAdminStateAction(event, request.getStateAction());

        event = eventsRepository.save(event);

        EventFullDto result = eventMapper.toEventFullDto(event);
        enrichEventWithStats(result);

        return result;
    }

    @Override
    public List<EventDistanceDto> findEventsWithinRadius(
            Float lat, Float lon, Double radius) {

        validateCoordinates(lat, lon);

        if (radius == null || !Double.isFinite(radius) || radius <= 0) {
            throw new ValidationException("Радиус должен быть положительным числом");
        }

        List<Event> events = eventsRepository.findAll(
                EventSpecification.published()
        );

        return events.stream()
                .map(event -> {
                    LocationDto location = getLocationOrThrow(event.getLocationId());

                    double distance = calculateDistance(
                            lat,
                            lon,
                            location.getLat(),
                            location.getLon()
                    );

                    if (distance > radius) {
                        return null;
                    }

                    return EventDistanceDto.builder()
                            .eventId(event.getId())
                            .title(event.getTitle())
                            .annotation(event.getAnnotation())
                            .eventDate(event.getEventDate())
                            .paid(event.getPaid())
                            .distance(distance)
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(EventDistanceDto::getDistance))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DistanceDto getDistanceToEvent(Long eventId, Float lat, Float lon) {

        validateCoordinates(lat, lon);

        Event event = eventsRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Событие с id=" + eventId + " не найдено")
                );

        LocationDto location = getLocationOrThrow(event.getLocationId());

        double distance = calculateDistance(
                lat,
                lon,
                location.getLat(),
                location.getLon()
        );

        return DistanceDto.builder()
                .eventId(eventId)
                .distance(distance)
                .build();
    }

    @Override
    public EventFullDto updateUserEvent(UserEventPathParams params, UpdateEventUserRequest dto) {
        Event event = eventsRepository.findByIdAndInitiatorId(params.getEventId(), params.getUserId())
                .orElseThrow(() -> new NotFoundException("Event с id=" + params.getEventId() + " не найден"));

        if (event.getState() == State.PUBLISHED) {
            throw new ConflictException("Можно изменять только отмененные события или события в состоянии ожидания модерации");
        }

        if (dto.getEventDate() != null) {
            LocalDateTime eventDate = parseAndValidateEventDate(
                    dto.getEventDate(),
                    "Дата события должна быть не ранее чем через 2 часа от текущего момента"
            );
            event.setEventDate(eventDate);
        }

        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());

        if (dto.getCategory() != null) {
            getCategoryOrThrow(dto.getCategory());
            event.setCategoryId(dto.getCategory()); // храним ID, а не объект
        }

        if (dto.getLocation() != null) {
            Long locationId = dto.getLocation().getId();

            locationClient.getLocationById(locationId);
            event.setLocationId(locationId);
        }

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
                default -> throw new ValidationException("Неизвестное состояние: " + dto.getStateAction());
            }
        }

        event = eventsRepository.save(event);

        EventFullDto result = eventMapper.toEventFullDto(event);
        enrichEventWithStats(result);

        return result;
    }

    private Map<Long, Long> getViewsMap(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/ewm/events/" + id)
                .toList();

        ResponseEntity<Object> response = statsClient.getStats(
                LocalDateTime.of(2000, 1, 1, 0, 0),
                LocalDateTime.now(),
                uris,
                true
        );

        if (response.getBody() == null) {
            return Collections.emptyMap();
        }

        List<ViewStatsDto> stats = objectMapper.convertValue(
                response.getBody(),
                new TypeReference<List<ViewStatsDto>>() {}
        );

        if (stats == null || stats.isEmpty()) {
            return Collections.emptyMap();
        }

        return stats.stream()
                .collect(Collectors.toMap(
                        stat -> Long.parseLong(
                                stat.getUri().substring(stat.getUri().lastIndexOf('/') + 1)
                        ),
                        ViewStatsDto::getHits,
                        (existing, replacement) -> existing
                ));
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            Map<Long, Long> counts =
                    requestClient.getConfirmedRequestsCount(eventIds);

            return counts != null ? counts : Collections.emptyMap();
        } catch (FeignException e) {
            log.warn("Не удалось получить количество заявок: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private void enrichEventWithStats(EventFullDto dto) {
        Map<Long, Long> viewsMap = getViewsMap(List.of(dto.getId()));
        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(List.of(dto.getId()));

        dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L));
        dto.setConfirmedRequests(confirmedMap.getOrDefault(dto.getId(), 0L));
    }

    private void enrichEventsWithStats(List<EventShortDto> dtos) {
        if (dtos.isEmpty()) {
            return;
        }

        List<Long> eventIds = dtos.stream()
                .map(EventShortDto::getId)
                .toList();

        Map<Long, Long> viewsMap = getViewsMap(eventIds);
        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(eventIds);

        dtos.forEach(dto -> {
            dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L));
            dto.setConfirmedRequests(confirmedMap.getOrDefault(dto.getId(), 0L));
        });
    }

    private void enrichEventFullDtosWithStats(List<EventFullDto> dtos) {
        if (dtos.isEmpty()) {
            return;
        }

        List<Long> eventIds = dtos.stream()
                .map(EventFullDto::getId)
                .toList();

        Map<Long, Long> viewsMap = getViewsMap(eventIds);
        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(eventIds);

        dtos.forEach(dto -> {
            dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L));
            dto.setConfirmedRequests(confirmedMap.getOrDefault(dto.getId(), 0L));
        });
    }

    private LocalDateTime parseAndValidateEventDate(String dateStr, String errorMessage) {
        LocalDateTime eventDate;
        try {
            eventDate = LocalDateTime.parse(dateStr, FORMATTER);
        } catch (Exception e) {
            throw new ValidationException("Неверный формат даты. Ожидается формат: " + DATE_FORMAT);
        }

        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException(errorMessage);
        }

        return eventDate;
    }

    private UserShortDto getUserOrThrow(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("User с id=" + userId + " не найден");
        }
    }

    private CategoryDto getCategoryOrThrow(Long categoryId) {
        try {
            return categoryClient.getCategoryById(categoryId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Category с id=" + categoryId + " не найдена");
        }
    }

    private void updateAdminEventFields(Event event, UpdateEventAdminRequest request) {
        if (request.getEventDate() != null) {
            LocalDateTime eventDate;
            try {
                eventDate = LocalDateTime.parse(request.getEventDate(), FORMATTER);
            } catch (Exception e) {
                throw new ValidationException("Неверный формат даты. Ожидается формат: " + DATE_FORMAT);
            }

            if (event.getState() == State.PUBLISHED && eventDate.isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConflictException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }

            if (eventDate.isBefore(LocalDateTime.now())) {
                throw new ValidationException("Дата события не может быть в прошлом");
            }

            event.setEventDate(eventDate);
        }

        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());

        if (request.getCategory() != null) {
            getCategoryOrThrow(request.getCategory());
            event.setCategoryId(request.getCategory());
        }

        if (request.getLocation() != null) {
            Long locationId = request.getLocation().getId();

            locationClient.getLocationById(locationId);
            event.setLocationId(locationId);
        }
    }

    private void handleAdminStateAction(Event event, StateAction stateAction) {
        if (stateAction == null) {
            return;
        }

        switch (stateAction) {
            case PUBLISH_EVENT -> {
                if (event.getState() != State.PENDING) {
                    throw new ConflictException("Событие должно находиться в состоянии ожидания публикации");
                }
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            case REJECT_EVENT -> {
                if (event.getState() == State.PUBLISHED) {
                    throw new ConflictException("Нельзя отклонить опубликованное событие");
                }
                event.setState(State.CANCELED);
            }
            default -> throw new ValidationException("Неизвестное состояние: " + stateAction);
        }
    }

    private List<EventShortDto> getEventsSortedByDate(Specification<Event> spec, PublicEventParams params) {
        PageRequest pageRequest = PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                Sort.by(Sort.Direction.ASC, "eventDate")
        );

        Page<Event> page = eventsRepository.findAll(spec, pageRequest);
        List<Event> events = page.getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<EventShortDto> dtos = eventMapper.toEventShortDtoList(events);
        enrichEventsWithStats(dtos);

        return dtos;
    }

    private List<EventShortDto> getEventsSortedByViews(Specification<Event> spec, PublicEventParams params) {
        List<Event> allEvents = eventsRepository.findAll(spec);

        if (allEvents.isEmpty()) {
            return Collections.emptyList();
        }

        List<EventShortDto> dtos = eventMapper.toEventShortDtoList(allEvents);
        enrichEventsWithStats(dtos);

        dtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());

        int from = params.getFrom();
        int size = params.getSize();

        if (from >= dtos.size()) {
            return Collections.emptyList();
        }

        int toIndex = Math.min(from + size, dtos.size());
        return dtos.subList(from, toIndex);
    }

    private void sendHitToStats(String uri, String ip) {
        statsClient.saveHit(EndpointHitDto.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private void validateCoordinates(Float lat, Float lon) {
        if (lat == null || lon == null) {
            throw new ValidationException("Координаты lat и lon обязательны");
        }

        if (!Float.isFinite(lat) || lat < -90 || lat > 90) {
            throw new ValidationException("Широта должна быть от -90 до 90");
        }

        if (!Float.isFinite(lon) || lon < -180 || lon > 180) {
            throw new ValidationException("Долгота должна быть от -180 до 180");
        }
    }

    private LocationDto getLocationOrThrow(Long locationId) {
        try {
            return locationClient.getLocationById(locationId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException(
                    "Локация с id=" + locationId + " не найдена"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EventShortDto getInternalEvent(Long eventId) {
        return getInternalEvents(List.of(eventId)).get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getInternalEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }

        List<Long> uniqueIds = eventIds.stream()
                .distinct()
                .toList();

        List<Event> events = eventsRepository.findAllById(uniqueIds);

        Map<Long, Event> eventsById = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        event -> event
                ));

        if (!eventsById.keySet().containsAll(uniqueIds)) {
            throw new NotFoundException(
                    "Одно или несколько событий не найдены"
            );
        }

        List<Long> categoryIds = events.stream()
                .map(Event::getCategoryId)
                .distinct()
                .toList();

        List<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();

        Map<Long, CategoryDto> categoriesById =
                categoryClient.getCategoriesByIds(categoryIds)
                        .stream()
                        .collect(Collectors.toMap(
                                CategoryDto::getId,
                                category -> category
                        ));

        Map<Long, UserShortDto> usersById =
                userClient.getUsersByIds(userIds)
                        .stream()
                        .collect(Collectors.toMap(
                                UserShortDto::getId,
                                user -> user
                        ));

        List<EventShortDto> result = uniqueIds.stream()
                .map(eventsById::get)
                .map(event -> {
                    EventShortDto dto = eventMapper.toEventShortDto(event);

                    CategoryDto category = categoriesById.get(event.getCategoryId());
                    UserShortDto initiator = usersById.get(event.getInitiatorId());

                    if (category == null || initiator == null) {
                        throw new NotFoundException(
                                "Не удалось получить категорию или инициатора события"
                        );
                    }

                    dto.setCategory(category);
                    dto.setInitiator(initiator);

                    return dto;
                })
                .toList();

        return result;
    }
}