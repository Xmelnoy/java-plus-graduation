package ewm.comments.service;

import dto.EventShortDto;
import dto.UserShortDto;
import ewm.client.EventClient;
import ewm.client.UserClient;
import ewm.comments.dto.CommentDto;
import ewm.comments.dto.NewCommentDto;
import ewm.comments.dto.UpdateCommentDto;
import ewm.comments.mapper.CommentMapper;
import ewm.comments.model.Comment;
import ewm.comments.repository.CommentRepository;
import exception.ConflictException;
import exception.NotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final CommentMapper commentMapper;

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        log.info("Добавление комментария от пользователя id={} к событию id={}", userId, eventId);

        UserShortDto author = getUserOrThrow(userId);
        EventShortDto event = getEventOrThrow(eventId);

//        if (!event.getState().name().equals("PUBLISHED")) {
//            throw new ConflictException("Нельзя оставить комментарий к неопубликованному событию");
//        }

        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());

        comment.setAuthorId(userId);
        comment.setEventId(eventId);
        comment.setCreatedOn(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toCommentDto(savedComment, author, event);
    }

    @Transactional
    @Override
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateDto) {
        log.info("Обновление комментария id={} пользователем id={}", commentId, userId);

        getUserOrThrow(userId);
        Comment comment = getCommentOrThrow(commentId);

        if (!comment.getAuthorId().equals(userId)) {
            throw new ConflictException("Вы не можете редактировать чужой комментарий");
        }

        comment.setText(updateDto.getText());
        return commentMapper.toCommentDto(commentRepository.save(comment), null, null);
    }

    @Transactional
    @Override
    public void deleteCommentByUser(Long userId, Long commentId) {
        log.info("Удаление комментария id={} пользователем id={}", commentId, userId);

        getUserOrThrow(userId);
        Comment comment = getCommentOrThrow(commentId);

        if (!comment.getAuthorId().equals(userId)) {
            throw new ConflictException("Вы не можете удалить чужой комментарий");
        }

        commentRepository.deleteById(commentId);
    }

    @Transactional
    @Override
    public void deleteCommentByAdmin(Long commentId) {
        log.info("Удаление комментария id={} администратором", commentId);
        getCommentOrThrow(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getCommentsByEvent(Long eventId, Integer from, Integer size) {
        log.info("Получение комментариев к событию id={}", eventId);
        getEventOrThrow(eventId);

        PageRequest pageRequest = PageRequest.of(from / size, size);

        return commentRepository.findAllByEventId(eventId, pageRequest)
                .stream()
                .map(comment -> {
                    UserShortDto author = getUserOrThrow(comment.getAuthorId());
                    EventShortDto event = getEventOrThrow(comment.getEventId());
                    return commentMapper.toCommentDto(comment, author, event);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto getCommentById(Long userId, Long commentId) {
        log.info("Получение комментария по id={} пользователем id={}", commentId, userId);

        getUserOrThrow(userId);
        Comment comment = getCommentOrThrow(commentId);

        if (!comment.getAuthorId().equals(userId)) {
            throw new ConflictException("Вы не можете посмотреть чужой комментарий через приватный эндпоинт");
        }

        UserShortDto author = getUserOrThrow(comment.getAuthorId());
        EventShortDto event = getEventOrThrow(comment.getEventId());
        return commentMapper.toCommentDto(comment, author, event);
    }

    @Transactional
    @Override
    public CommentDto updateCommentByAdmin(Long commentId, UpdateCommentDto updateDto) {
        log.info("Обновление комментария id={} администратором", commentId);

        Comment comment = getCommentOrThrow(commentId);

        if (updateDto.getText() != null && !updateDto.getText().isBlank()) {
            comment.setText(updateDto.getText());
        }

        Comment saved = commentRepository.save(comment);
        UserShortDto author = getUserOrThrow(saved.getAuthorId());
        EventShortDto event = getEventOrThrow(saved.getEventId());
        return commentMapper.toCommentDto(saved, author, event);
    }

    @Override
    public CommentDto getCommentByIdForAdmin(Long commentId) {
        log.info("Получение комментария по id={} администратором", commentId);
        Comment comment = getCommentOrThrow(commentId);
        UserShortDto author = getUserOrThrow(comment.getAuthorId());
        EventShortDto event = getEventOrThrow(comment.getEventId());
        return commentMapper.toCommentDto(comment, author, event);
    }

    private UserShortDto getUserOrThrow(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private EventShortDto getEventOrThrow(Long eventId) {
        try {
            return eventClient.getEventById(eventId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден"));
    }
}