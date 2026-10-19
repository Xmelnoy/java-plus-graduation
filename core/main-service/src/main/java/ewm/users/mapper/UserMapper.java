package ewm.users.mapper;

import org.mapstruct.Mapper;
import ewm.users.dto.NewUserRequest;
import ewm.users.dto.UserDto;
import ewm.users.dto.UserShortDto;
import ewm.users.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);

    User toUser(NewUserRequest newUserRequest);

    List<UserDto> toUserDtoList(List<User> users);
}