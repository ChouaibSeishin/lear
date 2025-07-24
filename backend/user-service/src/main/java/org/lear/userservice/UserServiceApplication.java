package org.lear.userservice;

import org.lear.userservice.entities.Role;
import org.lear.userservice.entities.RoleName;
import org.lear.userservice.entities.User;
import org.lear.userservice.repositories.RoleRepository;
import org.lear.userservice.repositories.UserRepository;
import org.lear.userservice.security.PasswordEncoderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = "org.lear")
public class UserServiceApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);

    }

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoderConfig passwordEncoderConfig;

    @Override
    public void run(String... args) throws Exception {}


}
