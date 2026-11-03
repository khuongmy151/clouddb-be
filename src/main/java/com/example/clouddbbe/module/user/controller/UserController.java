package com.example.clouddbbe.module.user.controller;


import com.example.clouddbbe.module.user.dto.BanUserRequest;
import com.example.clouddbbe.module.user.dto.ChangePasswordRequest;
import com.example.clouddbbe.module.user.dto.CreateUserRequest;
import com.example.clouddbbe.module.user.dto.UserResponse;
import com.example.clouddbbe.module.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public UserResponse getViewProfile(@PathVariable("username") String username){
        UserResponse response = userService.viewProfile(username);
        return ResponseEntity.ok(response).getBody();
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


    @PatchMapping("/change-password")
    public ResponseEntity<UserResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        UserResponse response = userService.changePassword(username, request);
        return ResponseEntity.ok(response);
    }

}
