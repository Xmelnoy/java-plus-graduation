package ewm.events.service;

import ewm.events.dto.*;
import ewm.events.dto.params.AdminEventParams;
import ewm.events.dto.params.PaginationParams;
import ewm.events.dto.params.PublicEventParams;
import ewm.events.dto.params.UserEventPathParams;
import ewm.participationRequest.dto.EventRequestStatusUpdateRequest;
import ewm.participationRequest.dto.EventRequestStatusUpdateResult;
import ewm.participationRequest.dto.ParticipationRequestDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface EventsService {

    List<EventShortDto> getEvents(PublicEventParams params, HttpServletRequest request);

    EventFullDto getEvent(Long id, HttpServletRequest request);

    EventFullDto createEvent(Long userId, NewEventDto dto);

    List<EventShortDto> getUserEvents(Long userId, PaginationParams params, HttpServletRequest request);

    EventFullDto getUserEvent(UserEventPathParams params);

    EventFullDto updateUserEvent(UserEventPathParams params, UpdateEventUserRequest dto);

    List<EventFullDto> getAdminEvents(AdminEventParams params);

    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest request);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest request);
}
