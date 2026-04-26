package com.app.ecommerce.userservice.repository;

import com.app.ecommerce.userservice.model.AppRole;
import com.app.ecommerce.userservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);
}
