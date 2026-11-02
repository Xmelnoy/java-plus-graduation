package ewm.users.controller;

import dto.UserShortDto;
import ewm.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserShortDto> getUserById(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping
    public ResponseEntity<List<UserShortDto>> getUsersByIds(
            @RequestParam("ids") List<Long> ids
    ) {
        return ResponseEntity.ok(userService.getUsersByIds(ids));
    }
}