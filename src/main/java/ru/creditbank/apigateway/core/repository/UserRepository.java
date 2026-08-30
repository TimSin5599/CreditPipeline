package ru.creditbank.apigateway.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.creditbank.apigateway.core.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {
    Optional<UserModel> findByEmail(String email);
}
