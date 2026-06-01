package ma.project.echallenge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ThemeRequest {

    @NotBlank(message = "Le nom du thème est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;
}