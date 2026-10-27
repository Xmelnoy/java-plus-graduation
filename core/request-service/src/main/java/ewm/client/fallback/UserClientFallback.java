package ewm.client.fallback;

import dto.UserShortDto;
import ewm.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserShortDto getUserById(Long id) {
        log.warn("users-service недоступен. Возвращаем заглушку для пользователя id={}", id);
        UserShortDto dto = new UserShortDto();
        dto.setId(id);
        dto.setName("Unknown");
        return dto;
    }
}