package com.manish.auth.auth_app_backend.auth.services;

import com.manish.auth.auth_app_backend.auth.payload.UserDto;

public interface AuthService {
    UserDto registerUser(UserDto userDto);

}
