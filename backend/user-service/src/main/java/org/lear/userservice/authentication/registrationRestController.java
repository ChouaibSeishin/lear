package org.lear.userservice.authentication;


import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lear.userservice.dtos.UserDto;
import org.lear.userservice.entities.User;
import org.lear.userservice.mapper.UserMapper;
import org.lear.userservice.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class registrationRestController {
    private final RegistrationService registrationService;
    private final UserService userService;
    private final UserMapper userMapper;


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register(@RequestBody @Valid RegistrationRequest registrationRequest) throws Exception {
        registrationService.register(registrationRequest);
        return ResponseEntity.accepted().build();
    }


    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request) {
        return ResponseEntity.ok(registrationService.authenticate(request));
    }

    @PostMapping("/activate-account")
    public void confirm(@RequestParam String token) throws MessagingException {
        registrationService.activateAccount(token);
    }

    @GetMapping("/load-user")
    public ResponseEntity<UserDto> loadUser(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();  // this is the "sub" claim
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(userMapper.userToUserDto(user));
    }

@GetMapping("/users")
public ResponseEntity<List<UserDto>> loadUsers() {

    List<User> users = userService.getUsers();
     List<UserDto> userDtos = new ArrayList<>() ;
    users.forEach(user -> {

        userDtos.add(userMapper.userToUserDto(user));
    });

    return ResponseEntity.ok(userDtos);
}



}
