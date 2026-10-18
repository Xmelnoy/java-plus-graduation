package ewm.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCommentDto {

    @NotBlank
    @Size(min = 5, max = 2000)
    private String text;
}
