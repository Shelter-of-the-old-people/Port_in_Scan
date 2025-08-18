package com.example.port_in_scan.global.filter;

import com.example.port_in_scan.domain.member.auth.entity.CustomUserDetails;
import com.example.port_in_scan.domain.member.entity.User;
import com.example.port_in_scan.domain.member.repository.UserRepository;
import com.example.port_in_scan.global.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String NO_CHECK_URL = "/login"; // "/login"으로 들어오는 요청은 Filter 작동 X
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        /*
        로그인 요청이 들어오면 필터 통과하고 다음 호출로 넘어간다.
        이때, return 을 통해 다음 필터를 호출하고 현재 필터의 진행을 막는다.
         */
        if (request.getRequestURI().equals(NO_CHECK_URL)) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
        헤더에서 리프레시 토큰 추출한다.
        만약 토큰이 없다면 null 반환
         */
        String refreshToken = jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        if (refreshToken != null) {
            checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
            return; // RefreshToken 보낸 경우에는 AccessToken 재발급 하고 인증 처리는 하지 않게 하기위해 바로 return -> 필터 진행 막기
        } else {
            checkAccessTokenAndAuthentication(request, response, filterChain);
        }
    }

    /*
    [리프레시 토큰으로 유저 정보 찾기 & 액세스 토큰/리프레시 토큰 재발급 메소드]
    파라미터로 들어온 헤더에서 추출한 리프레시 토큰으로 DB 에서 유저를 찾고, 해당 유저가 있다면
    JwtService.createAccessToken()으로 AccessToken 생성,
    reIssueRefreshToken()로 리프레시 토큰 재발급 & DB에 리프레시 토큰 업데이트 메소드 호출
    그 후 JwtService.sendAccessTokenAndRefreshToken()으로 응답 헤더에 보내기
    */
    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
//                    String reIssuedRefreshToken = reIssueRefreshToken(user);
                    jwtService.sendAccessAndRefreshToken(response, jwtService.createAccessToken(user.getEmail(), user.getRole().getRoleName()));
                });
    }

    /*
    [리프레시 토큰 재발급 & DB에 리프레시 토큰 업데이트 메소드]
    jwtService.createRefreshToken()으로 리프레시 토큰 재발급 후
    DB에 재발급한 리프레시 토큰 업데이트 후 Flush
    */
    private String reIssueRefreshToken(User user) {
        String reIssuedRefreshToken = jwtService.createRefreshToken();
        user.initRefreshToken(reIssuedRefreshToken);
        userRepository.saveAndFlush(user);
        return reIssuedRefreshToken;
    }

    /*
    [액세스 토큰 체크 & 인증 처리 메소드]
    request -> extractAccessToken()으로 액세스 토큰 추출 후, isTokenValid()로 유효한 토큰인지 검증
    유효한 토큰이면, 액세스 토큰에서 extractId로 Id을 추출한 후 findByUserId()로 해당 id를 사용하는 유저 객체 반환
    그 유저 객체를 saveAuthentication()으로 인증 처리하여
    인증 허가 처리된 객체를 SecurityContextHolder  담기
    그 후 다음 인증 필터로 진행
    */
    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain filterChain) throws ServletException, IOException {

        Optional<User> user = jwtService.extractAccessToken(request)
                .filter(jwtService::isTokenValid)
                .flatMap(jwtService::extractId)
                .flatMap(userRepository::findByEmail);

        user.ifPresent(this::saveAuthentication);;

        filterChain.doFilter(request, response);
    }

    /*
    UserDetails 의 User Builder 생성 후 해당 객체를 인증 처리하여
    해당 유저 객체를 SecurityContextHolder 에 담아 인증 처리를 진행
    */
    public void saveAuthentication(User user) {

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(customUserDetails, null,
                        authoritiesMapper.mapAuthorities(customUserDetails.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
