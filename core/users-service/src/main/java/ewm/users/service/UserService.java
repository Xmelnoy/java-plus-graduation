package ewm.users.service;

import dto.UserShortDto;
import ewm.users.dto.NewUserRequest;
import ewm.users.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest newUserRequest);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);

    UserShortDto getUserById(Long userId);

    List<UserShortDto> getUsersByIds(List<Long> userIds);
}