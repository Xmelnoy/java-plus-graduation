package ewm.comments.controller;

import ewm.comments.dto.CommentDto;
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
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteCommentByAdmin(@PathVariable @Positive Long commentId) {
        log.info("DELETE /admin/comments/{} - Удаление комментария администратором", commentId);
        commentService.deleteCommentByAdmin(commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateCommentByAdmin(
            @PathVariable @Positive Long commentId,
            @Valid @RequestBody UpdateCommentDto updateCommentDto
    ) {
        log.info("PATCH /admin/comments/{} - Обновление комментария администратором", commentId);
        CommentDto updatedComment = commentService.updateCommentByAdmin(commentId, updateCommentDto);
        return ResponseEntity.ok(updatedComment);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getCommentById(@PathVariable @Positive Long commentId) {
        log.info("GET /admin/comments/{} - Получение комментария администратором", commentId);
        CommentDto comment = commentService.getCommentByIdForAdmin(commentId);
        return ResponseEntity.ok(comment);
    }
}