package ru.creditbank.apigateway.routes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import ru.creditbank.apigateway.core.Role;
import ru.creditbank.apigateway.core.UserModel;
import ru.creditbank.apigateway.jwt.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:credit-routing-security-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class CreditRoutingSecurityTest {
    private static final String CREDIT_APPLICATION_PATH = "/credit-service/api/v1/credit/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void routeCreditApplication_noToken_isUnauthorized() throws Exception {
        mockMvc.perform(post(CREDIT_APPLICATION_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void routeCreditApplication_adminToken_isForbidden() throws Exception {
        UserModel admin = UserModel.builder().id("admin-id").email("admin@ya.ru").role(Role.ADMIN).build();
        String token = jwtService.generateToken(admin);

        mockMvc.perform(post(CREDIT_APPLICATION_PATH)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
