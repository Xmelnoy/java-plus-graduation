package ewm.participationRequest.service;

import enums.ParticipationStatus;
import dto.EventShortDto;
import exception.ConflictException;
import exception.NotFoundException;

import ewm.client.EventClient;
import ewm.client.UserClient;
import ewm.participationRequest.dto.EventRequestStatusUpdateRequest;
import ewm.participationRequest.dto.EventRequestStatusUpdateResult;
import ewm.participationRequest.dto.EventRequestsCountDto;
import ewm.participationRequest.dto.ParticipationRequestDto;
import ewm.participationRequest.mapper.ParticipationRequestMapper;
import ewm.participationRequest.model.ParticipationRequest;

import ewm.participationRequest.repository.ParticipationRequestsRepository;
import dto.UserShortDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestsRepository requestRepository;
    private final ParticipationRequestMapper requestMapper;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение заявок пользователя id={}", userId);

        getUserOrThrow(userId);

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        log.info("Создание заявки от пользователя id={} на событие id={}", userId, eventId);

        UserShortDto user = getUserOrThrow(userId);
        EventShortDto event = getEventOrThrow(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        if (!"PUBLISHED".equals(event.getState())) {
            throw new ConflictException("Событие еще не опубликовано");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Запрос уже существует");
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED);
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedRequests) {
            throw new ConflictException("Достигнут лимит участников на данное событие");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setRequesterId(userId);
        request.setEventId(eventId);
        request.setCreated(LocalDateTime.now());

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(ParticipationStatus.CONFIRMED);
        } else {
            request.setStatus(ParticipationStatus.PENDING);
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        return requestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена заявки id={} пользователем id={}", requestId, userId);

        getUserOrThrow(userId);
        ParticipationRequest request = getRequestOrThrow(requestId);

        if (!request.getRequesterId().equals(userId)) {
            throw new ConflictException("Можно отменить только свою заявку");
        }

        request.setStatus(ParticipationStatus.CANCELED);
        requestRepository.save(request);

        return requestMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Получение заявок на событие id={} для организатора id={}", eventId, userId);

        EventShortDto event = getEventOrThrow(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь не является организатором события");
        }

        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(
            Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        log.info("Обновление статуса заявок на событие id={} от организатора id={}", eventId, userId);

        EventShortDto event = getEventOrThrow(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь не является организатором события");
        }

        List<Long> requestIds = updateRequest.getRequestIds();
        if (requestIds == null || requestIds.isEmpty()) {
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(List.of())
                    .build();
        }

        List<ParticipationRequest> requests = requestRepository.findAllByIdIn(requestIds);

        boolean hasNotPending = requests.stream()
                .anyMatch(r -> r.getStatus() != ParticipationStatus.PENDING);
        if (hasNotPending) {
            throw new ConflictException("Статус можно изменить только у заявок в состоянии ожидания");
        }

        ParticipationStatus targetStatus = updateRequest.getStatus();
        if (targetStatus != ParticipationStatus.CONFIRMED && targetStatus != ParticipationStatus.REJECTED) {
            throw new ConflictException("Доступные статусы только CONFIRMED/REJECTED");
        }

        if (targetStatus == ParticipationStatus.REJECTED) {
            requests.forEach(r -> r.setStatus(ParticipationStatus.REJECTED));
            List<ParticipationRequest> saved = requestRepository.saveAll(requests);
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(saved.stream().map(requestMapper::toDto).toList())
                    .build();
        }

        Integer limit = event.getParticipantLimit();
        if (limit != null && limit > 0) {
            long alreadyConfirmed = requestRepository.countByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED);
            long available = limit - alreadyConfirmed;

            if (available <= 0) {
                throw new ConflictException("Достигнут лимит по заявкам");
            }

            List<ParticipationRequest> toConfirm = new ArrayList<>();
            List<ParticipationRequest> toReject = new ArrayList<>();

            for (ParticipationRequest r : requests) {
                if (available > 0) {
                    r.setStatus(ParticipationStatus.CONFIRMED);
                    toConfirm.add(r);
                    available--;
                } else {
                    r.setStatus(ParticipationStatus.REJECTED);
                    toReject.add(r);
                }
            }

            requestRepository.saveAll(requests);

            long totalConfirmed = alreadyConfirmed + toConfirm.size();
            if (totalConfirmed >= limit) {
                List<ParticipationRequest> pending = requestRepository.findAllByEventIdAndStatus(
                        eventId, ParticipationStatus.PENDING);
                pending.forEach(r -> r.setStatus(ParticipationStatus.REJECTED));
                requestRepository.saveAll(pending);
            }

            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(toConfirm.stream().map(requestMapper::toDto).toList())
                    .rejectedRequests(toReject.stream().map(requestMapper::toDto).toList())
                    .build();
        }

        requests.forEach(r -> r.setStatus(ParticipationStatus.CONFIRMED));
        List<ParticipationRequest> saved = requestRepository.saveAll(requests);
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(saved.stream().map(requestMapper::toDto).toList())
                .rejectedRequests(List.of())
                .build();
    }

    @Override
    public List<EventRequestsCountDto> getConfirmedRequestsCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }
        return requestRepository.getConfirmedRequests(eventIds);
    }

    private UserShortDto getUserOrThrow(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Пользователь не найден: user_id = " + userId);
        }
    }

    private EventShortDto getEventOrThrow(Long eventId) {
        try {
            return eventClient.getEventById(eventId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Событие не найдено: event_id = " + eventId);
        }
    }

    private ParticipationRequest getRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена: request_id = " + requestId));
    }
}