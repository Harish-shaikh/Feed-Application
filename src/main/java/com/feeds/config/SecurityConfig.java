package com.feeds.config;

import java.util.Collection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	// User Creation
	@Bean
	public UserDetailsService userDetailsService() {
		return new CustomUserDetail();
	}

	// Configuring HttpSecurity
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(req -> req.requestMatchers("/**").permitAll().requestMatchers("/api1/user/**")
						.hasAnyAuthority("USER").requestMatchers("/api2/admin/**").hasAnyAuthority("ADMIN").anyRequest()
						.authenticated())
				.formLogin(login -> login
						.loginPage("/login").loginProcessingUrl("/login").successHandler((request, response, authentication) -> {
			                // Get the authorities (roles) of the authenticated user
			                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
			                // Check if the user has USER role
			                if (authorities.stream().anyMatch(r -> r.getAuthority().equals("USER"))) {
			                    response.sendRedirect("/api1/user/allpost");
			                } else if (authorities.stream().anyMatch(r -> r.getAuthority().equals("ADMIN"))) {
			                    response.sendRedirect("/api2/admin/updatedeletePost");
			                } else {
			                    // Handle other roles or scenarios
			                    response.sendRedirect("/login"); // You can set a default URL here
			                }
			            }))
				.logout(logout -> logout.permitAll())
				
				
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login?loginRequired=true")))
				.build();
	}

	// Password Encoding
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(userDetailsService());
		authenticationProvider.setPasswordEncoder(passwordEncoder());
		return authenticationProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

}