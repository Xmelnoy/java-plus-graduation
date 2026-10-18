package ewm.location.mapper;

import ewm.location.dto.LocationDto;
import ewm.location.dto.NewLocationDto;
import ewm.location.dto.UpdateLocationDto;
import ewm.location.model.Location;
import ewm.location.model.LocationType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {

    GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "center", source = ".", qualifiedByName = "toPoint")
    @Mapping(target = "locationType", source = "locationType", qualifiedByName = "stringToLocationType")
    @Mapping(target = "createdAt", ignore = true)
    Location toEntity(NewLocationDto dto);

    @Mapping(target = "locationType", source = "locationType", qualifiedByName = "locationTypeToString")
    LocationDto toDto(Location location);

    @Named("toPoint")
    default Point toPoint(NewLocationDto dto) {
        if (dto == null || dto.getLon() == null || dto.getLat() == null) {
            return null;
        }
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(dto.getLon(), dto.getLat()));
        point.setSRID(4326);
        return point;
    }

    @Named("toPointFromUpdate")
    default Point toPointFromUpdate(UpdateLocationDto dto) {
        if (dto == null || dto.getLon() == null || dto.getLat() == null) {
            return null;
        }
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(dto.getLon(), dto.getLat()));
        point.setSRID(4326);
        return point;
    }

    @Named("stringToLocationType")
    default LocationType stringToLocationType(String value) {
        if (value == null || value.isBlank()) return null;
        return LocationType.valueOf(value.toUpperCase());
    }

    @Named("locationTypeToString")
    default String locationTypeToString(LocationType value) {
        return value != null ? value.name() : null;
    }
}