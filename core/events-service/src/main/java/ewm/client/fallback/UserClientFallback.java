package ewm.client.fallback;

import dto.UserShortDto;
import ewm.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserShortDto getUserById(Long id) {
        log.warn("users-service недоступен. Возвращаем заглушку для пользователя id={}", id);
        UserShortDto fallback = new UserShortDto();
        fallback.setId(id);
        fallback.setName("Unknown User");
        return fallback;
    }

    @Override
    public List<UserShortDto> getUsersByIds(List<Long> userIds) {
        log.warn("users-service недоступен. Возвращаем заглушки для пользователей");
        return userIds.stream()
                .map(id -> {
                    UserShortDto user = new UserShortDto();
                    user.setId(id);
                    user.setName("Unknown User");
                    return user;
                })
                .toList();
    }
}