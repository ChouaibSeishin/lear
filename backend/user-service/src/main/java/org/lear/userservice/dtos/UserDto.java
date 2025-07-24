package org.lear.userservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lear.userservice.entities.RoleName;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String code;
    private RoleName roleName;
    private boolean accountLocked;
    private boolean enabled;


}
