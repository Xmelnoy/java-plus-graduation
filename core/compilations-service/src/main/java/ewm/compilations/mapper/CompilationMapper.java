package ewm.compilations.mapper;

import ewm.compilations.dto.NewCompilationDto;
import ewm.compilations.model.Compilation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ewm.compilations.dto.CompilationDto;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "events", ignore = true)
    CompilationDto toCompilationDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventIds", ignore = true)
    Compilation toCompilationFromNewDto(NewCompilationDto dto);
}
