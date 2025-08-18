package com.example.port_in_scan.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.port_in_scan.domain.member.auth.handler.LoginFailureHandler;
import com.example.port_in_scan.domain.member.auth.handler.LoginSuccessHandler;
import com.example.port_in_scan.domain.member.auth.handler.LogoutSuccessCustomHandler;
import com.example.port_in_scan.domain.member.auth.service.CustomUserDetailService;
import com.example.port_in_scan.domain.member.repository.UserRepository;
import com.example.port_in_scan.global.JwtService;
import com.example.port_in_scan.global.filter.CustomUsernamePasswordAuthenticationFilter;
import com.example.port_in_scan.global.filter.JwtAuthenticationFilter;
import com.example.port_in_scan.global.filter.LoginFilter;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final CustomUserDetailService customUserDetailService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final LogoutSuccessCustomHandler logoutSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers("/v1/**").hasRole("USER") // v1 경로는 인증된 유저 등급 이상 접근 가능
                        .requestMatchers("/v3/**").hasRole("ADMIN") // v3 경로는 관리자만 접근 가능
                        .anyRequest().permitAll())
                // 로그아웃 설정
                .logout((logout) -> logout
                        .logoutUrl("/logout").permitAll()
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .invalidateHttpSession(true)
                );

        http.addFilterAt(new LoginFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(customUsernamePasswordAuthenticationFilter(), LogoutFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter(), CustomUsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailService);

        return new ProviderManager(provider);
    }

    // BCrypt 암호화 메소드
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter() {
        CustomUsernamePasswordAuthenticationFilter customUsernamePasswordLoginFilter
                = new CustomUsernamePasswordAuthenticationFilter(objectMapper);
        customUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager());
        customUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        customUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return customUsernamePasswordLoginFilter;
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(jwtService, userRepository);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }


    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_USER");
    }

    @Bean
    public Filter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, userRepository);
    }
}