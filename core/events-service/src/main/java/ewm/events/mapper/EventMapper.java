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
    @Mapping(target = "locationId", source = "dto.location.id")
    Event toEvent(NewEventDto dto, CategoryDto category, UserShortDto initiator);

    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "location", ignore = true)
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    EventShortDto toEventShortDto(Event event);

    List<EventFullDto> toEventFullDtoList(List<Event> events);
    List<EventShortDto> toEventShortDtoList(List<Event> events);
}