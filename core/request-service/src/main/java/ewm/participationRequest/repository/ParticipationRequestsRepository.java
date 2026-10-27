package ewm.participationRequest.repository;

import ewm.events.model.Event;
import ewm.participationRequest.dto.EventRequestsCountDto;
import ewm.participationRequest.model.ParticipationRequest;
import ewm.participationRequest.model.ParticipationStatus;
import ewm.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParticipationRequestsRepository
        extends JpaRepository<ParticipationRequest, Long> {

    @Query("""
            select new ewm.participationRequest.dto.EventRequestsCountDto(
                r.event.id,
                count(r.id)
            )
            from ParticipationRequest r
            where r.status = ewm.participationRequest.model.ParticipationStatus.CONFIRMED
              and r.event.id in :eventIds
            group by r.event.id
            """)
    List<EventRequestsCountDto> getConfirmedRequests(@Param("eventIds") List<Long> eventIds);

    long countByEventIdAndStatus(Long eventId, ParticipationStatus status);

    List<ParticipationRequest> findByRequester(User requester);

    Long countByEventAndStatus(Event event, ParticipationStatus status);

    boolean existsByRequesterAndEvent(User requester, Event event);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    List<ParticipationRequest> findAllByEventIdAndStatus(Long eventId, ParticipationStatus status);
}
