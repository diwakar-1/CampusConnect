package com.campusconnect.security;

import com.campusconnect.entity.User;
import com.campusconnect.entity.UserStatus;
import com.campusconnect.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("No account found with email: " + email));

        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            throw new DisabledException("Your account registration is currently pending administrator approval. Please wait for confirmation.");
        }

        if (user.getStatus() == UserStatus.REJECTED) {
            throw new DisabledException("Your registration application was not approved. Please contact the event administrator.");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new LockedException("Your account has been suspended. Please contact the administrator.");
        }

        return new CustomUserDetails(user);
    }
}

