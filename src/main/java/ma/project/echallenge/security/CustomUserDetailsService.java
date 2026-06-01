package ma.project.echallenge.security;

import lombok.RequiredArgsConstructor;
import ma.project.echallenge.entity.Candidate;
import ma.project.echallenge.entity.User;
import ma.project.echallenge.repository.CandidateRepository;
import ma.project.echallenge.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Check candidate first
        Candidate candidate = candidateRepository.findByEmail(email).orElse(null);
        if (candidate != null) {
            return new org.springframework.security.core.userdetails.User(
                    candidate.getEmail(),
                    candidate.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CANDIDATE"))
            );
        }

        // Check admin user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}