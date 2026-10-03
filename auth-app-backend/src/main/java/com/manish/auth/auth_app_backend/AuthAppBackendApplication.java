package com.manish.auth.auth_app_backend;

import com.manish.auth.auth_app_backend.auth.configuration.AppConstants;
import com.manish.auth.auth_app_backend.auth.entities.Role;
import com.manish.auth.auth_app_backend.auth.repositories.RoleRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class AuthAppBackendApplication
        implements CommandLineRunner {

    @Autowired
    private RoleRepositories roleRepositories;

    public static void main(String[] args) {
        SpringApplication.run(AuthAppBackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //        we will create some default user roles
        //        ADMIN
        //        GUEST

        roleRepositories.findByName("ROLE_" + AppConstants.ADMIN_ROLE).ifPresentOrElse(role -> {
            System.out.println("Admin role already exists: " + role.getName());
        }, () -> {
            Role role = new Role();
            role.setName("ROLE_" + AppConstants.ADMIN_ROLE);
            role.setId(UUID.randomUUID());
            roleRepositories.save(role);
        });

        roleRepositories.findByName("ROLE_" + AppConstants.GUEST_ROLE).ifPresentOrElse(role -> {
            System.out.println("Guest role already exists: " + role.getName());
        }, () -> {
            Role role = new Role();
            role.setName("ROLE_" + AppConstants.GUEST_ROLE);
            role.setId(UUID.randomUUID());
            roleRepositories.save(role);
        });
    }
}
