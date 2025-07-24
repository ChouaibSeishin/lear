package org.lear.userservice.services;

import org.lear.userservice.authentication.RegistrationRequest;
import org.lear.userservice.dtos.UserDto;
import org.lear.userservice.entities.RoleName;
import org.lear.userservice.entities.User;

import java.util.List;


public interface UserService {
    User addRoleToUser(String email, RoleName rolename);
    List<User>getUsers();
    User getUserByEmail(String email);
    User getAuthenticatedUser();
    void updatePassword(String currentPassword, String newPassword);
    void updateUser(UserDto UserDto);

}
