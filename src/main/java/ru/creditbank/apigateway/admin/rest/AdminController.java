package ru.creditbank.apigateway.admin.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.creditbank.apigateway.registration.service.AuthService;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AuthService authService;

    @GetMapping("/users")
    public List<UserSummary> listUsers() {
        return authService.listUsers().stream()
                .map(UserSummary::from)
                .toList();
    }

    @PatchMapping("/users/{email}/promote")
    public ResponseEntity<Void> promoteToAdmin(@PathVariable String email) {
        authService.promoteToAdmin(email);
        return ResponseEntity.noContent().build();
    }
}
