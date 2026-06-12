package vn.edu.fpt.myfptschool.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.myfptschool.notification.dto.SendNotificationRequest;
import vn.edu.fpt.myfptschool.notification.dto.SendNotificationResponse;
import vn.edu.fpt.myfptschool.notification.service.AdminNotificationService;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Admin - Notifications")
@SecurityRequirement(name = "bearerAuth")
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Gửi thông báo (individual / classroom / all)")
    public ResponseEntity<SendNotificationResponse> send(
            Authentication authentication,
            @Valid @RequestBody SendNotificationRequest request
    ) {
        return ResponseEntity.ok(adminNotificationService.send(authentication.getName(), request));
    }
}
