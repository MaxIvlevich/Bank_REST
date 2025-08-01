package com.example.bankcards.security.impl;

import com.example.bankcards.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service implementation for loading user-specific data for Spring Security.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Locates the user based on the username.
     * Our User entity already implements UserDetails, so we can return it directly.
     *
     * @param username the username identifying the user whose data is required.
     * @return a fully populated user record (never {@code null}).
     * @throws UsernameNotFoundException if the user could not be found.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User Not Found with username: " + username));

    }
}
