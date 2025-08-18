package com.example.port_in_scan.domain.member.auth.service;

import com.github.tripko.domain.member.auth.entity.CustomUserDetails;
import com.github.tripko.domain.member.entity.User;
import com.github.tripko.domain.member.repository.UserRepository;
import com.github.tripko.exception.AppException;
import com.github.tripko.exception.ErrorCode;
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
