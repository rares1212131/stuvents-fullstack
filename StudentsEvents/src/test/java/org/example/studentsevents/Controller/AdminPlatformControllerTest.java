package org.example.studentsevents.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest; // <<< 1. USE THIS ANNOTATION
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Use @SpringBootTest to load the full application context.
@SpringBootTest
// @AutoConfigureMockMvc is still needed to provide the MockMvc bean.
@AutoConfigureMockMvc
class AdminPlatformControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // <<< 2. REMOVE ALL THE @MockBean ANNOTATIONS >>>
    // Because @SpringBootTest loads the REAL application, we no longer need to
    // provide mocks for the services or security components. Spring will create
    // the real beans, just like it does when you run the app normally.

    @Test
    void adminEndpoints_ShouldReturnUnauthorized_ForAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoints_ShouldReturnForbidden_ForNonAdminUser() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoints_ShouldReturnOk_ForAdminUser() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }
}