package ewm.comments.mapper;

import dto.EventShortDto;
import dto.UserShortDto;
import ewm.comments.dto.CommentDto;
import ewm.comments.dto.NewCommentDto;
import ewm.comments.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "text", source = "comment.text")
    @Mapping(target = "createdOn", source = "comment.createdOn")
    @Mapping(target = "eventId", source = "comment.eventId")
    @Mapping(target = "authorName", source = "user.name")
    CommentDto toCommentDto(Comment comment, UserShortDto user, EventShortDto event);

    @Mapping(target = "authorName", ignore = true)
    @Mapping(target = "eventId", source = "eventId")
    CommentDto toCommentDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    Comment toComment(NewCommentDto newCommentDto);
}