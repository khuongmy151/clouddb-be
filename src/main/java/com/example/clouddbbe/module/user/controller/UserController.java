package com.example.clouddbbe.module.user.controller;


import com.example.clouddbbe.module.user.dto.CreateUserRequest;
import com.example.clouddbbe.module.user.dto.UserResponse;
import com.example.clouddbbe.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

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


}
