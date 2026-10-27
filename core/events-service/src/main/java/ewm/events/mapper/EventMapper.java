package ewm.events.mapper;

import dto.CategoryDto;
import ewm.events.dto.EventFullDto;
import dto.EventShortDto;
import ewm.events.dto.NewEventDto;
import ewm.events.model.Event;
import dto.LocationDto;
import dto.UserShortDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "eventDate", ignore = true)
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "initiatorId", source = "initiator.id")
    @Mapping(target = "location.lat", source = "dto.location.lat")
    @Mapping(target = "location.lon", source = "dto.location.lon")
    Event toEvent(NewEventDto dto, CategoryDto category, UserShortDto initiator);

    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    EventShortDto toEventShortDto(Event event);

    List<EventFullDto> toEventFullDtoList(List<Event> events);
    List<EventShortDto> toEventShortDtoList(List<Event> events);

    @Mapping(target = "id", ignore = true)
    ewm.location.model.Location toLocation(LocationDto dto);
}