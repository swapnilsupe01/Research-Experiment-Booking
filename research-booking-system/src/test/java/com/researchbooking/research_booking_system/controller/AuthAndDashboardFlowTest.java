package com.researchbooking.research_booking_system.controller;

import com.researchbooking.research_booking_system.entity.User;
import com.researchbooking.research_booking_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class AuthAndDashboardFlowTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // --- 1. First Screen: Login Page Tests ---

    @Test
    void root_whenUnauthenticated_rendersLoginPageFirst() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth-login"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sign In")));
    }

    @Test
    void ui_whenUnauthenticated_rendersLoginPageFirst() throws Exception {
        mockMvc.perform(get("/ui"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth-login"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sign In")));
    }

    @Test
    void loginPage_isAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth-login"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sign In")));
    }

    // --- 2. Sign Up Page Tests & Validation ---

    @Test
    void signUpPage_isAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth-signup"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Create Student Account")));
    }

    @Test
    void signUp_missingRequiredFields_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"\",\"fullName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Full name, email and password are required."));
    }

    @Test
    void signUp_invalidEmailFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Test Student\",\"email\":\"invalid-email-format\",\"password\":\"Password123!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid email format."));
    }

    @Test
    void signUp_mismatchedConfirmPassword_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Test Student\",\"email\":\"student.new@example.com\",\"password\":\"Password123!\",\"confirmPassword\":\"MismatchPassword!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password and confirm password must match."));
    }

    @Test
    @Transactional
    void signUp_duplicateEmail_returnsConflict() throws Exception {
        String uniqueEmail = "dup." + UUID.randomUUID() + "@example.com";

        // First registration
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"First User\",\"email\":\"" + uniqueEmail + "\",\"password\":\"Pass12345!\"}"))
                .andExpect(status().isCreated());

        // Duplicate registration
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Second User\",\"email\":\"" + uniqueEmail + "\",\"password\":\"Pass12345!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email is already registered."));
    }

    @Test
    @Transactional
    void signUp_cannotCreateTeacherRole_evenIfRequestedInPayload() throws Exception {
        String uniqueEmail = "hacker." + UUID.randomUUID() + "@example.com";

        // Attempting to send role=TEACHER in payload
        MvcResult result = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Hacker\",\"email\":\"" + uniqueEmail + "\",\"password\":\"Secret123!\",\"role\":\"TEACHER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andReturn();

        // Verify in database entity
        User savedUser = userRepository.findByEmail(uniqueEmail).orElseThrow();
        assertEquals("STUDENT", savedUser.role, "Public registration must only create STUDENT accounts");
        assertNotEquals("Secret123!", savedUser.passwordHash, "Passwords must be hashed");
        assertTrue(passwordEncoder.matches("Secret123!", savedUser.passwordHash), "BCrypt hash must match original password");
    }

    // --- 3. Login, Inactive Account, and Role-Based Redirection Tests ---

    @Test
    void login_withInvalidCredentials_redirectsToLoginWithError() throws Exception {
        mockMvc.perform(formLogin("/login").user("email", "nonexistent@example.com").password("wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=invalid"));
    }

    @Test
    @Transactional
    void login_withInactiveAccount_redirectsToLoginWithDisabledError() throws Exception {
        String email = "inactive." + UUID.randomUUID() + "@example.com";
        User inactiveUser = new User();
        inactiveUser.fullName = "Inactive User";
        inactiveUser.email = email;
        inactiveUser.passwordHash = passwordEncoder.encode("TestPass123!");
        inactiveUser.role = "STUDENT";
        inactiveUser.accountStatus = "INACTIVE";
        userRepository.save(inactiveUser);

        mockMvc.perform(formLogin("/login").user("email", email).password("TestPass123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=disabled"));
    }

    @Test
    @Transactional
    void login_withValidStudentCredentials_redirectsToStudentDashboard() throws Exception {
        String email = "student." + UUID.randomUUID() + "@example.com";
        User student = new User();
        student.fullName = "Valid Student";
        student.email = email;
        student.passwordHash = passwordEncoder.encode("ValidPass123!");
        student.role = "STUDENT";
        student.accountStatus = "ACTIVE";
        userRepository.save(student);

        mockMvc.perform(formLogin("/login").user("email", email).password("ValidPass123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/student"));
    }

    @Test
    @Transactional
    void login_withValidTeacherCredentials_redirectsToTeacherDashboard() throws Exception {
        String email = "teacher." + UUID.randomUUID() + "@example.com";
        User teacher = new User();
        teacher.fullName = "Valid Teacher";
        teacher.email = email;
        teacher.passwordHash = passwordEncoder.encode("TeacherPass123!");
        teacher.role = "RESEARCHER";
        teacher.accountStatus = "ACTIVE";
        userRepository.save(teacher);

        mockMvc.perform(formLogin("/login").user("email", email).password("TeacherPass123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/teacher"));
    }

    // --- 4. Server-Side Dashboard Role Access Control ---

    @Test
    void studentDashboard_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/dashboard/student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void studentDashboard_withStudentRole_returnsOk() throws Exception {
        mockMvc.perform(get("/dashboard/student"))
                .andExpect(status().isOk())
                .andExpect(view().name("student-dashboard"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Available Experiments")));
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void studentDashboard_withTeacherRole_returnsForbidden() throws Exception {
        mockMvc.perform(get("/dashboard/student"))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherDashboard_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/dashboard/teacher"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void teacherDashboard_withTeacherRole_returnsOk() throws Exception {
        mockMvc.perform(get("/dashboard/teacher"))
                .andExpect(status().isOk())
                .andExpect(view().name("teacher-dashboard"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("My Experiments")));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void teacherDashboard_withStudentRole_returnsForbidden() throws Exception {
        mockMvc.perform(get("/dashboard/teacher"))
                .andExpect(status().isForbidden());
    }

    // --- 5. Logout Tests ---

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void logout_invalidatesSessionAndRedirectsToLoginWithParam() throws Exception {
        mockMvc.perform(logout("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    // --- 6. Portal Entry Points & Server-Side Role Enforcement Tests ---

    @Test
    void loginPage_rendersStudentAndTeacherPortalTabs() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Student Login")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Teacher Login")));
    }

    @Test
    @Transactional
    void login_studentAccount_selectingTeacherPortal_rejectsRoleMismatch() throws Exception {
        String email = "student.portal." + UUID.randomUUID() + "@example.com";
        User student = new User();
        student.fullName = "Student User";
        student.email = email;
        student.passwordHash = passwordEncoder.encode("ValidPass123!");
        student.role = "STUDENT";
        student.accountStatus = "ACTIVE";
        userRepository.save(student);

        // Submitting with portal=teacher must be rejected on the server
        mockMvc.perform(post("/login")
                        .param("email", email)
                        .param("password", "ValidPass123!")
                        .param("portal", "teacher"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=role_mismatch_teacher&portal=teacher"));
    }

    @Test
    @Transactional
    void login_teacherAccount_selectingStudentPortal_rejectsRoleMismatch() throws Exception {
        String email = "teacher.portal." + UUID.randomUUID() + "@example.com";
        User teacher = new User();
        teacher.fullName = "Teacher User";
        teacher.email = email;
        teacher.passwordHash = passwordEncoder.encode("ValidPass123!");
        teacher.role = "TEACHER";
        teacher.accountStatus = "ACTIVE";
        userRepository.save(teacher);

        // Submitting with portal=student must be rejected on the server
        mockMvc.perform(post("/login")
                        .param("email", email)
                        .param("password", "ValidPass123!")
                        .param("portal", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=role_mismatch_student&portal=student"));
    }

    @Test
    @Transactional
    void login_studentAccount_withStudentPortal_redirectsToStudentDashboard() throws Exception {
        String email = "student.match." + UUID.randomUUID() + "@example.com";
        User student = new User();
        student.fullName = "Matching Student";
        student.email = email;
        student.passwordHash = passwordEncoder.encode("ValidPass123!");
        student.role = "STUDENT";
        student.accountStatus = "ACTIVE";
        userRepository.save(student);

        mockMvc.perform(post("/login")
                        .param("email", email)
                        .param("password", "ValidPass123!")
                        .param("portal", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/student"));
    }

    @Test
    @Transactional
    void login_teacherAccount_withTeacherPortal_redirectsToTeacherDashboard() throws Exception {
        String email = "teacher.match." + UUID.randomUUID() + "@example.com";
        User teacher = new User();
        teacher.fullName = "Matching Teacher";
        teacher.email = email;
        teacher.passwordHash = passwordEncoder.encode("ValidPass123!");
        teacher.role = "TEACHER";
        teacher.accountStatus = "ACTIVE";
        userRepository.save(teacher);

        mockMvc.perform(post("/login")
                        .param("email", email)
                        .param("password", "ValidPass123!")
                        .param("portal", "teacher"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/teacher"));
    }

    // --- 7. Upgraded Student Dashboard Section Tests ---

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void studentDashboard_containsAllRequiredSections() throws Exception {
        mockMvc.perform(get("/dashboard/student"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Available Experiments")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Experiment Details")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("My Bookings")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Booking Status Overview")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Demo Booking Experience Active")));
    }
}
