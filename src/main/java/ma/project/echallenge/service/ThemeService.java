package ma.project.echallenge.service;

import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.request.ThemeRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.entity.Theme;
import ma.project.echallenge.exception.BadRequestException;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.ThemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;

    @Transactional
    public ApiResponse<Theme> createTheme(ThemeRequest request) {
        if (themeRepository.existsByName(request.getName())) {
            throw new BadRequestException("Un thème avec ce nom existe déjà");
        }

        Theme theme = new Theme();
        theme.setName(request.getName());
        theme.setDescription(request.getDescription());

        Theme savedTheme = themeRepository.save(theme);

        return ApiResponse.success("Thème créé avec succès", savedTheme);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Theme>> getAllThemes() {
        List<Theme> themes = themeRepository.findAll();
        return ApiResponse.success("Thèmes récupérés", themes);
    }

    @Transactional(readOnly = true)
    public ApiResponse<Theme> getThemeById(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thème non trouvé"));

        return ApiResponse.success("Thème récupéré", theme);
    }

    @Transactional
    public ApiResponse<Theme> updateTheme(Long id, ThemeRequest request) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thème non trouvé"));

        // Vérifier si le nouveau nom existe déjà (sauf si c'est le même)
        if (!theme.getName().equals(request.getName()) &&
                themeRepository.existsByName(request.getName())) {
            throw new BadRequestException("Un thème avec ce nom existe déjà");
        }

        theme.setName(request.getName());
        theme.setDescription(request.getDescription());

        Theme updatedTheme = themeRepository.save(theme);

        return ApiResponse.success("Thème mis à jour", updatedTheme);
    }

    @Transactional
    public ApiResponse<String> deleteTheme(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thème non trouvé"));

        themeRepository.delete(theme);

        return ApiResponse.success("Thème supprimé avec succès", null);
    }
}