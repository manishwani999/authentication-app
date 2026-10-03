package com.manish.auth.auth_app_backend.auth.services.serviceImpl;

import com.manish.auth.auth_app_backend.auth.payload.UserDto;
import com.manish.auth.auth_app_backend.auth.services.AuthService;
import com.manish.auth.auth_app_backend.auth.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto registerUser(UserDto userDto) {

        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userService.createUser(userDto);
    }


}
