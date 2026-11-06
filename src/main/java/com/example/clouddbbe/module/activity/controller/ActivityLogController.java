package com.example.clouddbbe.module.activity.controller;


import com.example.clouddbbe.core.response.PaginationResponse;
import com.example.clouddbbe.module.activity.dto.ActivityLogResponse;
import com.example.clouddbbe.module.activity.entity.ActionType;
import com.example.clouddbbe.module.activity.service.ActivityLogService;
import com.example.clouddbbe.module.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activitylog")
public class ActivityLogController {


    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    public ActivityLogController(ActivityLogService activityLogService, UserRepository userRepository) {
        this.activityLogService = activityLogService;
        this.userRepository = userRepository;
    }



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<PaginationResponse<ActivityLogResponse>> getActivityLog(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) ActionType actionType,
            @RequestParam(required = false) Integer targetUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
//            @RequestParam(defaultValue = "") ,
//            @RequestParam(defaultValue = "desc") String sortDir

     ){
        // Validate sort field để tránh lỗi
//        String validSortBy = switch (sortBy.toLowerCase()) {
//            case "username", "fullname", "email", "role", "status", "createdat" -> sortBy;
//            default -> "createdAt";
//        };

//        Sort sort = sortDir.equalsIgnoreCase("asc")
//                ? Sort.by(validSortBy).ascending()
//                : Sort.by(validSortBy).descending();

        Pageable pageable = PageRequest.of(page, size);

        PaginationResponse<ActivityLogResponse> response = activityLogService.getActivityLog(userId, actionType, targetUserId, pageable);
        return ResponseEntity.ok(response);
    }


    /**
     * User xem nhật ký hoạt động của chính mình
     */
    @GetMapping("/me")
    public ResponseEntity<PaginationResponse<ActivityLogResponse>> getMyActivityLogs(
            @RequestParam(required = false) ActionType actionType,
            @RequestParam(required = false) Integer targetUserId,
            @RequestParam(required = false) Integer targetDbId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer currentUserId = extractUserId(authentication);

        PaginationResponse<ActivityLogResponse> result = activityLogService.getMyActivityLogs(
                currentUserId,
                actionType,
                targetUserId,
                targetDbId,
                pageable
        );

        return ResponseEntity.ok(result);
    }

    private Integer extractUserId(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Chưa đăng nhập");
        }

         String username = authentication.getName();
         return userRepository.findIdByUsername(username);
    }
}
