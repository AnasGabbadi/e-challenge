package ma.project.echallenge.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        userDetails = new User(
                "test@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CANDIDATE"))
        );
    }

    // ==================== SUCCESSFUL AUTHENTICATION TESTS ====================

    @Test
    void doFilterInternal_ShouldAuthenticateUser_WhenValidToken() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        String email = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(email, principal.getUsername());

        verify(tokenProvider, times(1)).validateToken(token);
        verify(tokenProvider, times(1)).getEmailFromToken(token);
        verify(userDetailsService, times(1)).loadUserByUsername(email);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldSetAuthenticationDetails_WhenValidToken() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        String email = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication().getDetails());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
    }

    // ==================== NO TOKEN TESTS ====================

    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenNoAuthorizationHeader() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(tokenProvider, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenAuthorizationHeaderEmpty() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(tokenProvider, never()).validateToken(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenAuthorizationHeaderWithoutBearer() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidPrefix token123");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(tokenProvider, never()).validateToken(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ==================== INVALID TOKEN TESTS ====================

    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenTokenInvalid() throws ServletException, IOException {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(tokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(tokenProvider, times(1)).validateToken(invalidToken);
        verify(tokenProvider, never()).getEmailFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldContinueFilterChain_WhenTokenValidationFails() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        when(tokenProvider.validateToken(anyString())).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ==================== EXCEPTION HANDLING TESTS ====================

    @Test
    void doFilterInternal_ShouldHandleException_WhenTokenProviderThrowsException() throws ServletException, IOException {
        // Arrange
        String token = "problematic.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenThrow(new RuntimeException("Token parsing error"));

        // Act & Assert - Should not throw exception, just log it
        assertDoesNotThrow(() ->
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)
        );

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldHandleException_WhenUserDetailsServiceThrowsException() throws ServletException, IOException {
        // Arrange
        String token = "valid.token";
        String email = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email))
                .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        assertDoesNotThrow(() ->
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)
        );

        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ==================== EDGE CASES ====================

    @Test
    void doFilterInternal_ShouldHandleBearerWithExtraSpaces() throws ServletException, IOException {
        // Arrange
        String token = "valid.token.here";

        when(request.getHeader("Authorization")).thenReturn("Bearer  " + token); // Extra space
        when(tokenProvider.validateToken(anyString())).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldExtractTokenCorrectly_WhenBearerPresent() throws ServletException, IOException {
        // Arrange
        String expectedToken = "expected.jwt.token";
        String authHeader = "Bearer " + expectedToken;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(tokenProvider.validateToken(expectedToken)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(expectedToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, times(1)).validateToken(expectedToken);
    }

    @Test
    void doFilterInternal_ShouldSetCorrectAuthorities_FromUserDetails() throws ServletException, IOException {
        // Arrange
        String token = "valid.token";
        String email = "admin@example.com";

        UserDetails adminUser = new User(
                email,
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(adminUser);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    void doFilterInternal_ShouldAlwaysCallFilterChain() throws ServletException, IOException {
        // Arrange - Various scenarios
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldClearSecurityContext_BeforeEachRequest() throws ServletException, IOException {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "old@user.com", null
                )
        );

        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - Old authentication should be replaced
        verify(filterChain, times(1)).doFilter(request, response);
    }
}