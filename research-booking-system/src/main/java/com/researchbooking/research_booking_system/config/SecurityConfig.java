
package com.researchbooking.research_booking_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;

    public SecurityConfig(
            CustomAuthenticationSuccessHandler successHandler,
            CustomAuthenticationFailureHandler failureHandler) {
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/ui", "/login", "/signup", "/register",
                    "/css/**", "/js/**", "/images/**", "/error"
                ).permitAll()
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/users/register"
                ).permitAll()
                .requestMatchers(
                    "/dashboard/teacher/**",
                    "/api/experiments/supervisor/**"
                ).hasRole("TEACHER")
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/experiments/**"
                ).hasRole("TEACHER")
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/experiments/**"
                ).hasRole("TEACHER")
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/experiments/**"
                ).hasRole("TEACHER")
                .requestMatchers(
                    "/dashboard/student/**"
                ).hasRole("STUDENT")
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/experiments/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/login"),
                    request -> request.getRequestURI().startsWith("/dashboard")
                )
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
