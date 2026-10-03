package com.manish.auth.auth_app_backend.auth.controller;

import com.manish.auth.auth_app_backend.auth.configuration.AppConstants;
import com.manish.auth.auth_app_backend.auth.payload.UserDto;
import com.manish.auth.auth_app_backend.auth.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    // create user api
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(userDto));
    }

    // get all user api
    @GetMapping()
    public ResponseEntity<Iterable<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // get user by email api
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    // delete user by id api
    @DeleteMapping("/{userId}")
    public String deleteUserById(@PathVariable String userId) {
        userService.deleteUser(userId);
        return "user deleted successfully!!!";
    }

    // get user by id
    @PreAuthorize("hasRole('" + AppConstants.ADMIN_ROLE + "')")
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // update user by id
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @RequestBody UserDto userDto,
            @PathVariable("userId") String userId
    ) {
        return ResponseEntity.ok(userService.updateUser(userDto, userId));
    }


}
