package org.lear.userservice.services;


import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;

import org.lear.userservice.dtos.UserDto;
import org.lear.userservice.entities.Role;
import org.lear.userservice.entities.RoleName;
import org.lear.userservice.entities.User;
import org.lear.userservice.entities.UserLog;
import org.lear.userservice.handler.GlobalExceptionHandler;
import org.lear.userservice.repositories.RoleRepository;
import org.lear.userservice.repositories.UserLogRepository;
import org.lear.userservice.repositories.UserRepository;
import org.lear.userservice.security.PasswordEncoderConfig;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoderConfig passwordEncoder;
    private final GlobalExceptionHandler handler;



    @Override
    public User addRoleToUser(String email, RoleName rolename) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        Optional<Role> optionalRole = roleRepository.findByRoleName(rolename);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        if (optionalRole.isEmpty()) {
            throw new RuntimeException("Role not found");
        }


        User user = optionalUser.get();
        user.setRole(optionalRole.get());
        return userRepository.save(user);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }


    @Override
    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("user not found");

        } else
            return user.get();
    }


    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("User not authenticated") {
            };
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwtToken) {
            String email = jwtToken.getSubject();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        throw new UsernameNotFoundException("Invalid user details");
    }


    @Override
    public void updatePassword(String currentPassword, String newPassword) {
         User user = getAuthenticatedUser();

            if (passwordEncoder.passwordEncoder().matches(currentPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.passwordEncoder().encode(newPassword));
                userRepository.save(user);
            } else {
                throw new BadCredentialsException("Password Incorrect");
            }

    }

    @Override
    public void updateUser(UserDto userDto) {
        Optional<User> userOptional = userRepository.findById(userDto.getUserId());
        if(userOptional.isPresent()){
            User user = userOptional.get();
            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setCode(userDto.getCode());
            user.setEmail(userDto.getEmail());
            user.setEnabled(userDto.isEnabled());
            user.setAccountLocked(userDto.isAccountLocked());
            user.setRole(roleRepository.findByRoleName(userDto.getRoleName()).get());
            userRepository.save(user);

        }
        else {
            throw new NotFoundException("User Not Found");
        }

    }

}

