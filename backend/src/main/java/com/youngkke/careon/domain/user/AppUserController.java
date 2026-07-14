package com.youngkke.careon.domain.user;

import com.youngkke.careon.domain.user.dto.AppLoginResponse;
import com.youngkke.careon.domain.user.dto.AppMeResponse;
import com.youngkke.careon.domain.user.dto.LoginRequest;
import com.youngkke.careon.domain.user.dto.RefreshTokenRequest;
import com.youngkke.careon.domain.user.dto.RefreshTokenResponse;
import com.youngkke.careon.domain.user.dto.UpdateAppProfileRequest;
import com.youngkke.careon.global.auth.CurrentUserId;
import com.youngkke.careon.global.dto.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/users")
@RequiredArgsConstructor
public class AppUserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AppLoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.loginApp(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@CurrentUserId Integer userId) {
        return ResponseEntity.ok(userService.logout(userId));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(userService.reissueToken(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AppMeResponse> me(@CurrentUserId Integer userId) {
        return ResponseEntity.ok(userService.getAppMe(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<MessageResponse> updateMe(
            @CurrentUserId Integer userId, @Valid @RequestBody UpdateAppProfileRequest request) {
        return ResponseEntity.ok(userService.updateAppProfile(userId, request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<MessageResponse> withdraw(@CurrentUserId Integer userId) {
        return ResponseEntity.ok(userService.withdraw(userId));
    }
}
