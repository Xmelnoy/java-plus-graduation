package ewm.participationRequest.model;

import ewm.events.model.Event;
import ewm.users.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "participation_requests")
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ParticipationStatus status;
}
