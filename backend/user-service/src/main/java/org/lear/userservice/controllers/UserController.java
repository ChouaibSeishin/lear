package org.lear.userservice.controllers;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lear.userservice.dtos.UserDto;
import org.lear.userservice.entities.User;
import org.lear.userservice.mapper.UserMapper;
import org.lear.userservice.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;




    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestParam String currentPassword,
                                            @RequestParam String newPassword) {
        try {
            userService.updatePassword(currentPassword, newPassword);
            return ResponseEntity.ok("Password updated successfully");
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Incorrect current password");
        }
    }


    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDto userDto) {
        try {
            userService.updateUser(userDto);
            return ResponseEntity.ok("User updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping()
    public ResponseEntity<?> getUserByEmail(@RequestParam String email){
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(userMapper.userToUserDto(user));
    }

}

