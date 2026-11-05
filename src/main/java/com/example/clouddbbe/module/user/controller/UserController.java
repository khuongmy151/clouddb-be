package com.example.clouddbbe.module.user.controller;


import com.example.clouddbbe.core.response.PaginationResponse;
import com.example.clouddbbe.module.user.dto.*;
import com.example.clouddbbe.module.user.entity.Role;
import com.example.clouddbbe.module.user.entity.Status;
import com.example.clouddbbe.module.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.Console;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }




    @PostMapping("/register")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request){
        UserResponse reponse = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }



    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{username}/ban")
    public ResponseEntity<UserResponse> banUser(
            @PathVariable("username") String username,
            @Valid @RequestBody BanUserRequest request
    ) {
        UserResponse response = userService.banUser(username, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{username}/unban")
    public ResponseEntity<UserResponse> unbanUser(
            @PathVariable("username") String username
    ) {
        UserResponse response = userService.unbanUser(username);
        return ResponseEntity.ok(response);
    }








    @GetMapping("/me")
    public UserResponse getViewProfile(Authentication authentication){

        String username = authentication.getName();

        UserResponse response = userService.viewProfile(username);
        return ResponseEntity.ok(response).getBody();
    }

    @PatchMapping("/change-password")
    public ResponseEntity<UserResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        UserResponse response = userService.changePassword(username, request);
        return ResponseEntity.ok(response);
    }





    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PaginationResponse<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        // Validate sort field để tránh lỗi
        String validSortBy = switch (sortBy.toLowerCase()) {
            case "username", "fullname", "email", "role", "status", "createdat" -> sortBy;
            default -> "createdAt";
        };

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(validSortBy).ascending()
                : Sort.by(validSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        PaginationResponse<UserResponse> response = userService.getUsers(keyword, role, status, pageable);
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        UserResponse response = userService.updateUser(username, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("username") String username,
            Authentication authentication
    ) {
        String currentUsername = authentication.getName();
        userService.deleteUser(username, currentUsername);
        return ResponseEntity.noContent().build();
    }



}
