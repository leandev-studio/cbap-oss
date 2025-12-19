package com.cbap.security.service;

import com.cbap.persistence.entity.Role;
import com.cbap.persistence.entity.User;
import com.cbap.persistence.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * UserDetailsService implementation for CBAP.
 */
@Service
public class CbapUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CbapUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(user.isLocked())
                .credentialsExpired(false)
                .disabled(user.getStatus() != User.UserStatus.ACTIVE)
                .build();
    }

    /**
     * Get authorities (roles) for the user.
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(Role::getRoleName)
                .map(roleName -> "ROLE_" + roleName.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
