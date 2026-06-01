package ma.project.echallenge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>("error", message, data);
    }

    // ✅ Ajouter la méthode isSuccess()
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(this.status);
    }

    // ✅ Méthode alternative pour vérifier l'erreur
    public boolean isError() {
        return "error".equalsIgnoreCase(this.status);
    }
}