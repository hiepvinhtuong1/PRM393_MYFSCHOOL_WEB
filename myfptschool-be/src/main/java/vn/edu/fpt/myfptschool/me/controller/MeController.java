package vn.edu.fpt.myfptschool.me.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.myfptschool.common.dto.ApiResponse;
import vn.edu.fpt.myfptschool.me.service.MeService;

@Slf4j
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {

    private final MeService meService;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<Object>> getProfile(Authentication authentication) {
        log.info("[MeController] getProfile called, username={}", authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(meService.getProfile(authentication.getName())));
    }
}
