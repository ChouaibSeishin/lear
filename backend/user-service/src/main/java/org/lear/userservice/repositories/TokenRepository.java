package org.lear.userservice.repositories;


import org.lear.userservice.entities.Token;
import org.lear.userservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token,Long> {
    Optional<Token> findByToken(String token);

    Optional<Token> findByUser(User user);
}
