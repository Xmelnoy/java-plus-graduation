package ewm.client;

import dto.UserShortDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users-srvice", path = "/users")
public interface UserClient {

    @GetMapping("/{id}")
    UserShortDto getUserById(@PathVariable("id") Long id);
}
