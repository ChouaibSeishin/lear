package org.lear.userservice.authentication;


import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.lear.userservice.email.EmailService;
import org.lear.userservice.email.EmailTemplate;
import org.lear.userservice.entities.Token;
import org.lear.userservice.entities.User;
import org.lear.userservice.exception.EmailAlreadyExistsException;
import org.lear.userservice.handler.BusinessErrorCodes;
import org.lear.userservice.repositories.RoleRepository;
import org.lear.userservice.repositories.TokenRepository;
import org.lear.userservice.repositories.UserRepository;
import org.lear.userservice.security.JwtService;
import org.lear.userservice.security.PasswordEncoderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final RoleRepository roleRepository;
    private final PasswordEncoderConfig passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    @Value("${application.mailing.frontend.activation-url}")
    private String confirmationUrl;

    public void register(RegistrationRequest registrationRequest) throws Exception {

        var userRole = roleRepository.findByRoleName(registrationRequest.getRoleName())
                //todo - better error handling
                .orElseThrow(() -> new IllegalStateException("role not found"));
        String email = registrationRequest.getEmail();


        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(BusinessErrorCodes.ACCOUNT_ALREADY_EXIST, "Email already exists");
        }

        var user = User.builder()

                .email(registrationRequest.getEmail())
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .password(passwordEncoder.passwordEncoder().encode(registrationRequest.getPassword()))
                .accountLocked(false)
                .code(registrationRequest.getCode())
                .enabled(false)
                .role(userRole)
                .build();
        userRepository.save(user);



        sendValidationEmail(user);

    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);
        emailService.sendEmail(user.getEmail()
                , user.getEmail()
                , EmailTemplate.ACTIVATE_ACCOUNT
                , confirmationUrl
                , newToken
                , "Account activation");
    }

    private String generateAndSaveActivationToken(User user) {
        // Generate a new activation code
        String generatedToken = generateActivationCode(6);

        // Check if the user already has an existing token
        Optional<Token> existingTokenOpt = tokenRepository.findByUser(user);

        // If an existing token is found, update it; otherwise, create a new token
        Token token;
        if (existingTokenOpt.isPresent()) {
            token = existingTokenOpt.get();
            token.setToken(generatedToken);
            token.setCreatedAt(LocalDateTime.now());
            token.setExpiresAt(LocalDateTime.now().plusMinutes(20));
        } else {
            token = Token.builder()
                    .token(generatedToken)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(20))
                    .user(user)
                    .build();
        }

        // Save the token (updates if it already exists)
        tokenRepository.save(token);

        return generatedToken;
    }


    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        System.out.println("Request:---" + request.getEmail());
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            System.out.println("Authentication successful for: " + request.getEmail());

            var user = ((UserDetails) auth.getPrincipal());

            User customUser = userRepository.findByEmail(user.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found after successful authentication"));

            var claims = new HashMap<String, Object>();
            claims.put("username", customUser.getEmail());

            System.out.println("Attempting to generate token for: " + user.getUsername());
            var jwtToken = jwtService.generateToken(user);
            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("Token generated for: " + user.getUsername());

            return AuthenticationResponse.builder()
                    .token(jwtToken.get("jwt"))
                    .build();

        } catch (BadCredentialsException | LockedException | DisabledException e) {
            System.err.println("Authentication specific exception caught: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Generic authentication failed exception caught. Printing stack trace for details:");
            throw new RuntimeException("Authentication failed", e);
        }
    }

    @Transactional
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token).orElseThrow(() -> new MessagingException("Invalid token"));
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new MessagingException("Activation expired. A new token sent to your email");
        }
        var user = userRepository.findById(savedToken.getUser().getUserId()).orElseThrow(() -> new UsernameNotFoundException("username not found"));
        user.setEnabled(true);
        userRepository.save(user);
        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }


}
