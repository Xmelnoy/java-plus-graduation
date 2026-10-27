package ewm.client;

import dto.UserShortDto;
import ewm.client.fallback.UserClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(name = "users-service", path = "/users", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/{id}")
    UserShortDto getUserById(@PathVariable("id") Long id);

    @GetMapping
    List<UserShortDto> getUsersByIds(@RequestParam("ids") List<Long> userIds);
}