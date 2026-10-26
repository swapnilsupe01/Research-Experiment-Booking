package com.researchbooking.research_booking_system.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void publicEndpoints_shouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/ui"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/experiments"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/experiments/slots/upcoming"))
                .andExpect(status().isOk());
    }

    @Test
    void supervisorEndpoints_withoutAuth_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/experiments/supervisor/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void supervisorEndpoints_withStudentRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/experiments/supervisor/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void experimentCreate_withStudentRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/experiments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void slotCreate_withStudentRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/experiments/1/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"capacity\":5}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registration_shouldBePubliclyAccessible() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
