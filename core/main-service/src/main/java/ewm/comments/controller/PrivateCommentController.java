package ewm.comments.controller;

import ewm.comments.dto.CommentDto;
import ewm.comments.dto.NewCommentDto;
import ewm.comments.dto.UpdateCommentDto;
import ewm.comments.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @PathVariable @Positive Long userId,
            @RequestParam @Positive Long eventId,
            @Valid @RequestBody NewCommentDto newCommentDto
    ) {
        log.info("POST /users/{}/comments?eventId={} - Добавление комментария", userId, eventId);
        CommentDto createdComment = commentService.createComment(userId, eventId, newCommentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long commentId,
            @Valid @RequestBody UpdateCommentDto updateCommentDto
    ) {
        log.info("PATCH /users/{}/comments/{} - Обновление комментария", userId, commentId);
        CommentDto updatedComment = commentService.updateComment(userId, commentId, updateCommentDto);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long commentId
    ) {
        log.info("DELETE /users/{}/comments/{} - Удаление комментария пользователем", userId, commentId);
        commentService.deleteCommentByUser(userId, commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getCommentById(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long commentId
    ) {
        log.info("GET /users/{}/comments/{} - Получение комментария пользователем", userId, commentId);
        CommentDto comment = commentService.getCommentById(userId, commentId);
        return ResponseEntity.ok(comment);
    }
}