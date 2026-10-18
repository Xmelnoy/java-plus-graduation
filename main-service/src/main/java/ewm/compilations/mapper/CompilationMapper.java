package ewm.compilations.mapper;

import ewm.compilations.dto.NewCompilationDto;
import ewm.compilations.model.Compilation;
import ewm.events.mapper.EventMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ewm.compilations.dto.CompilationDto;

@Mapper(componentModel = "spring", uses = EventMapper.class)
public interface CompilationMapper {

    CompilationDto toCompilationDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toCompilationFromNewDto(NewCompilationDto dto);
}
