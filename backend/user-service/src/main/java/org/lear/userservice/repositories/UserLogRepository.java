package org.lear.userservice.repositories;

import org.lear.userservice.entities.User;
import org.lear.userservice.entities.UserLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLogRepository extends JpaRepository<UserLog,Long> {
    List<UserLog> findByUser(User user) ;
}
