package com.example.port_in_scan.domain.member.service;

import com.github.tripko.domain.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder bCryptPasswordEncoder;

    // 회원가입
//    @Transactional
//    public void signup(SignupDto signupDto) {
//        // 유저 name 중복 확인
//        if (userRepository.existsByUsername(signupDto.getUsername())) {
//            throw new AppException(ErrorCode.USERNAME_DUPLICATED.getMessage(), ErrorCode.USERNAME_DUPLICATED);
//        }
//
//        // 유저 id 중복 확인
//        if (userRepository.existsByEmail(signupDto.getEmail())) {
//            throw new AppException(ErrorCode.USER_EMAIL_DUPLICATED.getMessage(), ErrorCode.USER_EMAIL_DUPLICATED);
//        }
//
//        User user = User.SignupToEntity(signupDto);
//
//        user.passwordEncode(bCryptPasswordEncoder);
//
//        userRepository.save(user);
//    }

}
