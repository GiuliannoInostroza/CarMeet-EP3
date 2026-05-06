package com.carmeet.ms_auth_user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carmeet.ms_auth_user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByRut(String rut);
}

