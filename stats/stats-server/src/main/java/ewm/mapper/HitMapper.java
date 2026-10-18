package ewm.mapper;

import org.mapstruct.Mapper;
import ewm.dto.EndpointHitDto;
import ewm.model.EndpointHit;

@Mapper(componentModel = "spring")
public interface HitMapper {

    EndpointHit toEntity(EndpointHitDto dto);

    EndpointHitDto toDto(EndpointHit entity);
}
