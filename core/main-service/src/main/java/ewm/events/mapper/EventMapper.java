package ewm.events.mapper;

import ewm.categories.dto.CategoryDto;
import ewm.categories.model.Category;
import ewm.events.dto.EventFullDto;
import ewm.events.dto.EventShortDto;
import ewm.location.dto.LocationDto;
import ewm.events.dto.NewEventDto;
import ewm.events.model.Event;
import ewm.location.model.Location;
import ewm.users.dto.UserShortDto;
import ewm.users.model.User;
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
    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "location", source = "dto.location")
    Event toEvent(NewEventDto dto, Category category, User initiator);

    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    EventShortDto toEventShortDto(Event event);

    List<EventFullDto> toEventFullDtoList(List<Event> events);
    List<EventShortDto> toEventShortDtoList(List<Event> events);

    CategoryDto toCategoryDto(Category category);
    UserShortDto toUserShortDto(User user);

    LocationDto toLocationDto(Location location);

    @Mapping(target = "id", ignore = true)
    Location toLocation(LocationDto dto);
}