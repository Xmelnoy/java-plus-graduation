package ewm.users.service;

import dto.UserShortDto;
import exception.ConflictException;
import exception.NotFoundException;
import ewm.users.dto.NewUserRequest;
import ewm.users.dto.UserDto;
import ewm.users.mapper.UserMapper;
import ewm.users.model.User;
import ewm.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new ConflictException("Пользователь с email=" + newUserRequest.getEmail()+ " уже существует");
        }
        User user = userMapper.toUser(newUserRequest);
        user = userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);

        if (ids == null || ids.isEmpty()) {
            return userMapper.toUserDtoList(userRepository.findAll(pageRequest).getContent());
        } else {
            return userMapper.toUserDtoList(userRepository.findByIdIn(ids, pageRequest));
        }
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserShortDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id=" + userId + " не найден"
                ));

        return userMapper.toUserShortDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserShortDto> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        List<Long> uniqueIds = userIds.stream()
                .distinct()
                .toList();

        Map<Long, User> usersById = userRepository.findAllById(uniqueIds)
                .stream()
                .collect(Collectors.toMap(
                        User::getId,
                        Function.identity()
                ));

        if (!usersById.keySet().containsAll(uniqueIds)) {
            throw new NotFoundException("Один или несколько пользователей не найдены");
        }

        return uniqueIds.stream()
                .map(usersById::get)
                .map(userMapper::toUserShortDto)
                .toList();
    }
}