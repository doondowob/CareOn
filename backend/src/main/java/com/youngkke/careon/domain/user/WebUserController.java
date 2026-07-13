package com.youngkke.careon.domain.user;

import com.youngkke.careon.domain.user.dto.AppInstallStatusRequest;
import com.youngkke.careon.domain.user.dto.LoginRequest;
import com.youngkke.careon.domain.user.dto.PasswordResetLinkRequest;
import com.youngkke.careon.domain.user.dto.PasswordResetRequest;
import com.youngkke.careon.domain.user.dto.SignupRequest;
import com.youngkke.careon.domain.user.dto.SignupResponse;
import com.youngkke.careon.domain.user.dto.UpdateWebProfileRequest;
import com.youngkke.careon.domain.user.dto.WebLoginResponse;
import com.youngkke.careon.domain.user.dto.WebMeResponse;
import com.youngkke.careon.global.auth.CurrentUserId;
import com.youngkke.careon.global.dto.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/web/users")
@RequiredArgsConstructor
public class WebUserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<WebLoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.loginWeb(request));
    }

    @GetMapping("/me")
    public ResponseEntity<WebMeResponse> me(@CurrentUserId Integer userId) {
        return ResponseEntity.ok(userService.getWebMe(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<MessageResponse> updateMe(
            @CurrentUserId Integer userId, @Valid @RequestBody UpdateWebProfileRequest request) {
        return ResponseEntity.ok(userService.updateWebProfile(userId, request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<MessageResponse> withdraw(@CurrentUserId Integer userId) {
        return ResponseEntity.ok(userService.withdraw(userId));
    }

    @PatchMapping("/me/app-install-status")
    public ResponseEntity<MessageResponse> updateAppInstallStatus(
            @CurrentUserId Integer userId, @Valid @RequestBody AppInstallStatusRequest request) {
        return ResponseEntity.ok(userService.updateAppInstallStatus(userId, request));
    }

    @PostMapping("/password/reset-link")
    public ResponseEntity<MessageResponse> sendPasswordResetLink(
            @Valid @RequestBody PasswordResetLinkRequest request) {
        return ResponseEntity.ok(userService.sendPasswordResetLink(request));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(userService.resetPassword(request));
    }
}
