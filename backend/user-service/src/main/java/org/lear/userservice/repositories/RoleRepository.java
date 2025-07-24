package org.lear.userservice.repositories;

import org.lear.userservice.entities.Role;
import org.lear.userservice.entities.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByRoleName(RoleName roleName);

}
