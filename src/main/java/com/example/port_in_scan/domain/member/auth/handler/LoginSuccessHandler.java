package com.example.port_in_scan.domain.member.auth.handler;

import com.example.port_in_scan.domain.member.entity.User;
import com.example.port_in_scan.domain.member.repository.UserRepository;
import com.example.port_in_scan.exception.AppException;
import com.example.port_in_scan.exception.ErrorCode;
import com.example.port_in_scan.global.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${jwt.access.expiration}")
    private String accessTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        String email = extractUsername(authentication); // 인증 정보에서 Username(id) 추출
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND.getMessage(), ErrorCode.USER_EMAIL_NOT_FOUND)
        );

        String accessToken = jwtService.createAccessToken(email, user.getRole().getRoleName()); // JwtService 의 createAccessToken 을 사용하여 AccessToken 발급
        String refreshToken = jwtService.createRefreshToken(); // JwtService 의 createRefreshToken 을 사용하여 RefreshToken 발급

        jwtService.sendAccessAndRefreshToken(response, accessToken); // 응답 헤더에 AccessToken, RefreshToken 실어서 응답

        user.initRefreshToken(refreshToken);
        userRepository.saveAndFlush(user);

        log.info("로그인에 성공하였습니다. email : {}", email);
        log.info("로그인에 성공하였습니다. AccessToken : {}", accessToken);
        log.info("발급된 AccessToken 만료 기간 : {}", accessTokenExpiration);
    }

    private String extractUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}