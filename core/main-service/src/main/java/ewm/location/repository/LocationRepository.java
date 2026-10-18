package ewm.location.repository;

import ewm.events.model.Event;
import ewm.location.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query(value = """
        SELECT l.* FROM locations l
        WHERE (6371000 * acos(least(1.0, greatest(-1.0,
            cos(radians(:lat)) * cos(radians(l.lat)) * cos(radians(l.lon) - radians(:lon)) +
            sin(radians(:lat)) * sin(radians(l.lat))
        )))) <= l.radius_meters
        """, nativeQuery = true)
    List<Location> findLocationsContainingPoint(
            @Param("lat") double lat,
            @Param("lon") double lon
    );

    @Query(value = """
        SELECT e.* FROM events e
        JOIN locations el ON e.location_id = el.id
        JOIN locations l ON l.id = :locationId
        WHERE (6371000 * acos(least(1.0, greatest(-1.0,
            cos(radians(l.lat)) * cos(radians(el.lat)) * cos(radians(el.lon) - radians(l.lon)) +
            sin(radians(l.lat)) * sin(radians(el.lat))
        )))) <= COALESCE(l.radius_meters, 0)
        """, nativeQuery = true)
    List<Event> findEventsInLocation(@Param("locationId") Long locationId);

    @Query(value = """
        SELECT e.id, e.title, e.annotation, e.event_date, e.paid,
               (6371000 * acos(least(1.0, greatest(-1.0,
                   cos(radians(:lat)) * cos(radians(l.lat)) * cos(radians(l.lon) - radians(:lon)) +
                   sin(radians(:lat)) * sin(radians(l.lat))
               )))) AS distance
        FROM events e
        JOIN locations l ON e.location_id = l.id
        WHERE e.state = 'PUBLISHED'
          AND (6371000 * acos(least(1.0, greatest(-1.0,
              cos(radians(:lat)) * cos(radians(l.lat)) * cos(radians(l.lon) - radians(:lon)) +
              sin(radians(:lat)) * sin(radians(l.lat))
          )))) <= :radius
        ORDER BY distance
        """, nativeQuery = true)
    List<Object[]> findEventsWithDistanceWithinRadius(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radius
    );

    @Query(value = """
        SELECT (6371000 * acos(least(1.0, greatest(-1.0,
            cos(radians(:lat)) * cos(radians(l.lat)) * cos(radians(l.lon) - radians(:lon)) +
            sin(radians(:lat)) * sin(radians(l.lat))
        ))))
        FROM events e
        JOIN locations l ON e.location_id = l.id
        WHERE e.id = :eventId
        """, nativeQuery = true)
    Double findDistanceToEvent(
            @Param("eventId") Long eventId,
            @Param("lat") double lat,
            @Param("lon") double lon
    );
}