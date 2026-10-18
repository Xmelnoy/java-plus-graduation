package ewm.location.repository;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ewm.events.model.Event;
import ewm.location.model.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query(value = """
        SELECT l.* FROM locations l
        WHERE ST_DWithin(l.center, :point, l.radius_meters)
        """, nativeQuery = true)
    List<Location> findLocationsContainingPoint(@Param("point") Point point);

    @Query(value = """
        SELECT e.* FROM events e
        JOIN locations l ON e.location_id = l.id
        WHERE l.id = :locationId
          AND ST_DWithin(
              ST_SetSRID(ST_MakePoint(l.lon, l.lat), 4326)::geography,
              l.center,
              l.radius_meters
          )
        """, nativeQuery = true)
    List<Event> findEventsInLocation(@Param("locationId") Long locationId);

    @Query(value = """
        SELECT e.id, e.title, e.annotation, e.event_date, e.paid,
               ST_Distance(
                   ST_SetSRID(ST_MakePoint(l.lon, l.lat), 4326)::geography,
                   ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
               ) as distance
        FROM events e
        JOIN locations l ON e.location_id = l.id
        WHERE e.state = 'PUBLISHED'
          AND ST_DWithin(
              ST_SetSRID(ST_MakePoint(l.lon, l.lat), 4326)::geography,
              ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
              :radius
          )
        ORDER BY distance
        """, nativeQuery = true)
    List<Object[]> findEventsWithDistanceWithinRadius(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radius
    );

    @Query(value = """
        SELECT ST_Distance(
            ST_SetSRID(ST_MakePoint(l.lon, l.lat), 4326)::geography,
            ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
        )
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