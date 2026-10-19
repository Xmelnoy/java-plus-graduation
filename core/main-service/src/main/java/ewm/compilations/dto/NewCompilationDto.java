package ewm.compilations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NewCompilationDto {

    private List<Long> events;
    private Boolean pinned = false;

    @NotBlank
    @Size(max = 50)
    private String title;
}