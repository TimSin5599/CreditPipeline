package ru.creditbank.apigateway.ping;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Diagnostic endpoint, not part of the README's package layout — there is no real
 * downstream service to proxy to yet, so this exists purely to exercise JwtGenerationFilter.
 */
@RestController
public class PingController {

    @GetMapping("/api/v1/ping")
    public Map<String, String> ping(Authentication authentication) {
        return Map.of("authenticatedAs", authentication.getName());
    }
}
