package org.lear.userservice.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.lear.userservice.entities.Role;
import org.lear.userservice.entities.RoleName;
import org.lear.userservice.entities.User;
import org.lear.userservice.repositories.RoleRepository;
import org.lear.userservice.repositories.UserRepository;
import org.lear.userservice.security.PasswordEncoderConfig;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoderConfig passwordEncoder;

    @PostConstruct
    public void initAdmin() {
        // Create ADMIN role and admin user if not present
        if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
            Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setRoleName(RoleName.ADMIN);
                        return roleRepository.save(role);
                    });

            User admin = new User();
            admin.setEmail("admin@admin.com");
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setPassword(passwordEncoder.passwordEncoder().encode("admin@123")); // ⚠️ Change in production
            admin.setAccountLocked(false);
            admin.setEnabled(true);
            admin.setCode("ADMIN001");
            admin.setRole(adminRole);

            userRepository.save(admin);
        }

        // Create USER role and regular user if not present
        if (userRepository.findByEmail("user@example.com").isEmpty()) {
            Role userRole = roleRepository.findByRoleName(RoleName.USER)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setRoleName(RoleName.USER);
                        return roleRepository.save(role);
                    });

            User user = new User();
            user.setEmail("user@example.com");
            user.setFirstName("Default");
            user.setLastName("User");
            user.setPassword(passwordEncoder.passwordEncoder().encode("user123")); // ⚠️ Change in production
            user.setAccountLocked(false);
            user.setEnabled(true);
            user.setCode("USER001");
            user.setRole(userRole);

            userRepository.save(user);
        }

    }
}