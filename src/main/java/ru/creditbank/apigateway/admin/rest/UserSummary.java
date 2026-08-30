package ru.creditbank.apigateway.admin.rest;

import ru.creditbank.apigateway.core.Role;
import ru.creditbank.apigateway.core.UserModel;

public record UserSummary(String email, Role role) {
    public static UserSummary from(UserModel user) {
        return new UserSummary(user.getEmail(), user.getRole());
    }
}
