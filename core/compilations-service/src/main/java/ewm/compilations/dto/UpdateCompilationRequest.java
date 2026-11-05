package ewm.compilations.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCompilationRequest {

    private List<Long> events;
    private Boolean pinned;

    @Size(max = 50)
    private String title;
}
