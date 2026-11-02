package ewm.client;

import ewm.client.fallback.UserClientFallback;
import dto.UserShortDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/internal/users", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/{id}")
    UserShortDto getUserById(@PathVariable("id") Long id);
}