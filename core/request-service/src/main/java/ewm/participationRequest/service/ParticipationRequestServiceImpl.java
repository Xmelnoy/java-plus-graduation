package ewm.participationRequest.service;

import ewm.events.model.Event;
import ewm.exception.ConflictException;
import ewm.exception.ValidationException;
import ewm.participationRequest.dto.ParticipationRequestDto;
import ewm.participationRequest.model.ParticipationRequest;
import ewm.participationRequest.model.ParticipationStatus;
import ewm.users.model.User;
import ewm.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ewm.exception.NotFoundException;
import ewm.participationRequest.repository.ParticipationRequestsRepository;
import ewm.participationRequest.mapper.ParticipationRequestMapper;
import ewm.events.repository.EventsRepository;
import ewm.events.model.State;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private static final String USER_NOT_FOUND = "Пользователь не найден: user_id = ";
    private static final String EVENT_NOT_FOUND = "Событие не найдено: event_id = ";
    private static final String REQUEST_NOT_FOUND = "Заявка не найдена: request_id = ";
    private static final String REQUEST_ALREADY_EXISTS = "Запрос уже существует";
    private static final String REQUEST_YOUR_EVENT = "Инициатор события не может добавить запрос на участие в своём событии";
    private static final String EVENT_NOT_PUBLISHED = "Событие еще не опубликовано";
    private static final String EVENT_OVER_LIMIT = "Достигнут лимит участников на данное событие";
    private static final String CANCEL_YOUR_REQUEST = "Можно отменить только свою заявку";

    private final UserRepository userRepository;
    private final EventsRepository eventRepository;
    private final ParticipationRequestsRepository requestRepository;
    private final ParticipationRequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
            () -> new ValidationException(USER_NOT_FOUND + userId)
        );

        List<ParticipationRequest> requests = requestRepository.findByRequester(user);

        return requests.stream()
            .map(requestMapper::toDto)
            .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(
            () -> new NotFoundException(USER_NOT_FOUND + userId)
        );

        Event event = eventRepository.findById(eventId).orElseThrow(
            () -> new NotFoundException(EVENT_NOT_FOUND + eventId)
        );

        ParticipationRequest request = new ParticipationRequest();
        Long confirmedRequests = requestRepository.countByEventAndStatus(event, ParticipationStatus.CONFIRMED);

        if (requestRepository.existsByRequesterAndEvent(user, event)) {
            throw new ConflictException(REQUEST_ALREADY_EXISTS);
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(REQUEST_YOUR_EVENT);
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException(EVENT_NOT_PUBLISHED);
        }

        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedRequests) {
            throw new ConflictException(EVENT_OVER_LIMIT);
        }

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(ParticipationStatus.CONFIRMED);
        } else {
            request.setStatus(ParticipationStatus.PENDING);
        }

        request.setRequester(user);
        request.setEvent(event);
        request.setCreated(LocalDateTime.now());

        ParticipationRequest saveRequest = requestRepository.save(request);

        return requestMapper.toDto(saveRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        User user = userRepository.findById(userId).orElseThrow(
            () -> new NotFoundException(USER_NOT_FOUND + userId)
        );

        ParticipationRequest request = requestRepository.findById(requestId).orElseThrow(
            () -> new NotFoundException(REQUEST_NOT_FOUND + requestId)
        );

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException(CANCEL_YOUR_REQUEST);
        }

        request.setStatus(ParticipationStatus.CANCELED);
        requestRepository.save(request);

        return requestMapper.toDto(request);
    }
}
