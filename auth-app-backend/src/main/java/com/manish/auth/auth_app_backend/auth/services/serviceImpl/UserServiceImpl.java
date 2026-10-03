package com.manish.auth.auth_app_backend.auth.services.serviceImpl;

import com.manish.auth.auth_app_backend.auth.configuration.AppConstants;
import com.manish.auth.auth_app_backend.auth.entities.Provider;
import com.manish.auth.auth_app_backend.auth.entities.Role;
import com.manish.auth.auth_app_backend.auth.entities.User;
import com.manish.auth.auth_app_backend.auth.helpers.UserHelper;
import com.manish.auth.auth_app_backend.auth.payload.UserDto;
import com.manish.auth.auth_app_backend.auth.repositories.RoleRepositories;
import com.manish.auth.auth_app_backend.auth.repositories.UserRepository;
import com.manish.auth.auth_app_backend.auth.services.UserService;
import com.manish.auth.auth_app_backend.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final RoleRepositories roleRepositories;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {

        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("User with given email already exists");
        }
        // if you have extra checks, you can write
        User user = modelMapper.map(userDto, User.class);
        // check provider
        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);
        // role assign here to user -- for authorization
        // TODO:
        Role role = roleRepositories.findByName("ROLE_" + AppConstants.GUEST_ROLE).orElse(null);
//        user.getRoles().add(role);
        if (user.getRoles() == null) {
            user.setRoles(new java.util.HashSet<>());
        }
        if (role != null) {
            user.getRoles().add(role);
        }

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given Email ID"));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User existingUser = userRepository
                .findById(uId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given ID"));

        // we are not going to change email id for this project
        if (userDto.getName() != null) existingUser.setName(userDto.getName());
        if (userDto.getImage() != null) existingUser.setImage(userDto.getImage());
        if (userDto.getProvider() != null) existingUser.setProvider(userDto.getProvider());

        // TODO: change the password update logic:
        if (userDto.getPassword() != null) existingUser.setPassword(userDto.getPassword());
        existingUser.setIsEnable(userDto.getIsEnable());
        existingUser.setUpdatedAt(Instant.now());
        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User user = userRepository.findById(uId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given ID"));
        userRepository.delete(user);

    }

    @Override
    public UserDto getUserById(String userId) {
        User user = userRepository.findById(UserHelper.parseUUID(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given ID"));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public Iterable<UserDto> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }
}
