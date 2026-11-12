package com.campusconnect.repository;

import com.campusconnect.entity.User;
import com.campusconnect.entity.UserRole;
import com.campusconnect.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatus(UserStatus status);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.status = :status")
    List<User> findByRoleAndStatus(@Param("roleName") UserRole roleName, @Param("status") UserStatus status);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRole(@Param("roleName") UserRole roleName);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRole(@Param("roleName") UserRole roleName);

    long countByStatus(UserStatus status);
}

