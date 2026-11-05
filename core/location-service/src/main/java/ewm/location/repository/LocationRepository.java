package ewm.location.repository;

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
}