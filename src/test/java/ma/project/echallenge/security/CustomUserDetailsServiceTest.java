package ma.project.echallenge.security;

import ma.project.echallenge.entity.Candidate;
import ma.project.echallenge.entity.User;
import ma.project.echallenge.repository.CandidateRepository;
import ma.project.echallenge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Candidate candidate;
    private User adminUser;

    @BeforeEach
    void setUp() {
        candidate = new Candidate();
        candidate.setId(1L);
        candidate.setEmail("candidate@test.com");
        candidate.setPassword("encodedPassword123");
        candidate.setFirstName("John");
        candidate.setLastName("Doe");

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("adminPassword456");
        adminUser.setRole(User.Role.ADMIN);
    }

    // ==================== LOAD CANDIDATE TESTS ====================

    @Test
    void loadUserByUsername_ShouldReturnCandidate_WhenCandidateExists() {
        // Arrange
        when(candidateRepository.findByEmail("candidate@test.com"))
                .thenReturn(Optional.of(candidate));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("candidate@test.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("candidate@test.com", userDetails.getUsername());
        assertEquals("encodedPassword123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CANDIDATE")));

        verify(candidateRepository, times(1)).findByEmail("candidate@test.com");
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void loadUserByUsername_ShouldHaveCorrectAuthority_ForCandidate() {
        // Arrange
        when(candidateRepository.findByEmail("candidate@test.com"))
                .thenReturn(Optional.of(candidate));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("candidate@test.com");

        // Assert
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CANDIDATE")));
    }

    // ==================== LOAD ADMIN USER TESTS ====================

    @Test
    void loadUserByUsername_ShouldReturnAdminUser_WhenCandidateNotFoundAndAdminExists() {
        // Arrange
        when(candidateRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(adminUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@test.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("admin@test.com", userDetails.getUsername());
        assertEquals("adminPassword456", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));

        verify(candidateRepository, times(1)).findByEmail("admin@test.com");
        verify(userRepository, times(1)).findByEmail("admin@test.com");
    }

    @Test
    void loadUserByUsername_ShouldHaveCorrectAuthority_ForAdmin() {
        // Arrange
        when(candidateRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(adminUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@test.com");

        // Assert
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    // ==================== USER NOT FOUND TESTS ====================

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(candidateRepository.findByEmail("nonexistent@test.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent@test.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("nonexistent@test.com")
        );

        assertTrue(exception.getMessage().contains("User not found"));
        assertTrue(exception.getMessage().contains("nonexistent@test.com"));

        verify(candidateRepository, times(1)).findByEmail("nonexistent@test.com");
        verify(userRepository, times(1)).findByEmail("nonexistent@test.com");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WithCorrectMessage() {
        // Arrange
        String email = "missing@test.com";
        when(candidateRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(email)
        );

        assertEquals("User not found: " + email, exception.getMessage());
    }

    // ==================== PRIORITY TESTS ====================

    @Test
    void loadUserByUsername_ShouldPrioritizeCandidate_WhenBothExist() {
        // Arrange - Both candidate and admin have same email (edge case)
        when(candidateRepository.findByEmail("duplicate@test.com"))
                .thenReturn(Optional.of(candidate));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("duplicate@test.com");

        // Assert - Should return candidate (checked first)
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CANDIDATE")));

        verify(candidateRepository, times(1)).findByEmail("duplicate@test.com");
        verify(userRepository, never()).findByEmail(anyString());
    }

    // ==================== EDGE CASES ====================

    @Test
    void loadUserByUsername_ShouldHandleEmptyEmail() {
        // Arrange
        when(candidateRepository.findByEmail("")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(""));
    }

    @Test
    void loadUserByUsername_ShouldHandleSpecialCharacters() {
        // Arrange
        String specialEmail = "test+user@example.com";
        candidate.setEmail(specialEmail);
        when(candidateRepository.findByEmail(specialEmail))
                .thenReturn(Optional.of(candidate));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(specialEmail);

        // Assert
        assertEquals(specialEmail, userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_ShouldReturnEnabledUser() {
        // Arrange
        when(candidateRepository.findByEmail("candidate@test.com"))
                .thenReturn(Optional.of(candidate));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("candidate@test.com");

        // Assert
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    void loadUserByUsername_ShouldWorkForDifferentUserRoles() {
        // Arrange
        User moderatorUser = new User();
        moderatorUser.setEmail("mod@test.com");
        moderatorUser.setPassword("modPass");
        moderatorUser.setRole(User.Role.ADMIN);

        when(candidateRepository.findByEmail("mod@test.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("mod@test.com"))
                .thenReturn(Optional.of(moderatorUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("mod@test.com");

        // Assert
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    void loadUserByUsername_ShouldCallRepositories_InCorrectOrder() {
        // Arrange
        when(candidateRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.empty());

        // Act
        try {
            customUserDetailsService.loadUserByUsername("test@test.com");
        } catch (UsernameNotFoundException e) {
            // Expected
        }

        // Assert - Verify order: candidate first, then user
        var inOrder = inOrder(candidateRepository, userRepository);
        inOrder.verify(candidateRepository).findByEmail("test@test.com");
        inOrder.verify(userRepository).findByEmail("test@test.com");
    }

    @Test
    void loadUserByUsername_ShouldReturnCorrectPasswordForAuthentication() {
        // Arrange
        String expectedPassword = "secureEncodedPassword";
        candidate.setPassword(expectedPassword);
        when(candidateRepository.findByEmail("candidate@test.com"))
                .thenReturn(Optional.of(candidate));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("candidate@test.com");

        // Assert
        assertEquals(expectedPassword, userDetails.getPassword());
    }
}