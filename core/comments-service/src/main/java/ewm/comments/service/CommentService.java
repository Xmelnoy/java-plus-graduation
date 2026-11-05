package ewm.comments.service;

import ewm.comments.dto.CommentDto;
import ewm.comments.dto.NewCommentDto;
import ewm.comments.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateDto);

    void deleteCommentByUser(Long userId, Long commentId);

    void deleteCommentByAdmin(Long commentId);

    List<CommentDto> getCommentsByEvent(Long eventId, Integer from, Integer size);

    CommentDto getCommentById(Long userId, Long commentId);

    CommentDto updateCommentByAdmin(Long commentId, UpdateCommentDto updateCommentDto);

    CommentDto getCommentByIdForAdmin(Long commentId);
}