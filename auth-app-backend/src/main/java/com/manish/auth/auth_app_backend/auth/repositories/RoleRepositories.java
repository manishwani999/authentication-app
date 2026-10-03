package com.manish.auth.auth_app_backend.auth.repositories;

import com.manish.auth.auth_app_backend.auth.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepositories extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
}
