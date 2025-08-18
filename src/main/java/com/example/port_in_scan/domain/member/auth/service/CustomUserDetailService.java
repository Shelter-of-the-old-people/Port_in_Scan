package com.example.port_in_scan.domain.member.auth.service;

import com.example.port_in_scan.domain.member.auth.entity.CustomUserDetails;
import com.example.port_in_scan.domain.member.entity.User;
import com.example.port_in_scan.domain.member.repository.UserRepository;
import com.example.port_in_scan.exception.AppException;
import com.example.port_in_scan.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EMAIL_UNMATCHED.getMessage(), ErrorCode.USER_EMAIL_UNMATCHED));

        return new CustomUserDetails(user);
    }
}
