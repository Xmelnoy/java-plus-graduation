package ewm.participationRequest.repository;

import enums.ParticipationStatus;
import ewm.participationRequest.dto.EventRequestsCountDto;
import ewm.participationRequest.model.ParticipationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestsRepository
        extends JpaRepository<ParticipationRequest, Long> {

    @Query("""
        select new ewm.participationRequest.dto.EventRequestsCountDto(
            r.eventId,
            count(r.id)
        )
        from ParticipationRequest r
        where r.status = :status
          and r.eventId in :eventIds
        group by r.eventId
        """)
    List<EventRequestsCountDto> getConfirmedRequests(
            @Param("eventIds") List<Long> eventIds,
            @Param("status") ParticipationStatus status
    );

    long countByEventIdAndStatus(Long eventId, ParticipationStatus status);

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    boolean existsByRequesterIdAndEventIdAndStatus(Long requesterId, Long eventId, ParticipationStatus status);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    List<ParticipationRequest> findAllByEventIdAndStatus(Long eventId, ParticipationStatus status);

    List<ParticipationRequest> findAllByIdIn(List<Long> ids);

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);
}