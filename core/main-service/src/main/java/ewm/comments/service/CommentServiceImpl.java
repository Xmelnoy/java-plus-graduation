package ewm.comments.service;

import ewm.comments.dto.CommentDto;
import ewm.comments.dto.NewCommentDto;
import ewm.comments.dto.UpdateCommentDto;
import ewm.comments.mapper.CommentMapper;
import ewm.comments.model.Comment;
import ewm.comments.repository.CommentRepository;
import ewm.events.model.Event;
import ewm.events.model.State;
import ewm.events.repository.EventsRepository;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.users.model.User;
import ewm.users.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final EventsRepository eventRepository;
    private final CommentMapper commentMapper;

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        log.info("Добавление комментария от пользователя id={} к событию id={}", userId, eventId);

        User author = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Нельзя оставить комментарий к неопубликованному событию");
        }

        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreatedOn(LocalDateTime.now());

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateDto) {
        log.info("Обновление комментария id={} пользователем id={}", commentId, userId);

        getUserOrThrow(userId);
        Comment comment = getCommentOrThrow(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Вы не можете редактировать чужой комментарий");
        }

        comment.setText(updateDto.getText());

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public void deleteCommentByUser(Long userId, Long commentId) {
        log.info("Удаление комментария id={} пользователем id={}", commentId, userId);

        getUserOrThrow(userId);
        Comment comment = getCommentOrThrow(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
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
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto getCommentById(Long userId, Long commentId) {
        log.info("Получение комментария по id={} пользователем id={}", commentId, userId);

        getUserOrThrow(userId);
        Comment comment = getCommentOrThrow(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Вы не можете посмотреть чужой комментарий через приватный эндпоинт");
        }

        return commentMapper.toCommentDto(comment);

    }

    @Transactional
    @Override
    public CommentDto updateCommentByAdmin(Long commentId, UpdateCommentDto updateDto) {
        log.info("Обновление комментария id={} администратором", commentId);

        Comment comment = getCommentOrThrow(commentId);

        if (updateDto.getText() != null && !updateDto.getText().isBlank()) {
            comment.setText(updateDto.getText());
        }

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден"));
    }

    @Override
    public CommentDto getCommentByIdForAdmin(Long commentId) {
        log.info("Получение комментария по id={} администратором", commentId);
        Comment comment = getCommentOrThrow(commentId);
        return commentMapper.toCommentDto(comment);
    }
}