package com.youngkke.careon.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ERD의 "회원(user)" 테이블. PostgreSQL에서 USER는 예약어라 테이블명은 users로 매핑한다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "region", length = 20)
    private String region;

    @Column(name = "terms_agreed", nullable = false)
    private boolean termsAgreed;

    @Column(name = "install_prompt_count", nullable = false)
    private int installPromptCount;

    @Column(name = "app_installed", nullable = false)
    private boolean appInstalled;

    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled;

    @Column(name = "reset_token", length = 255)
    private String resetToken;

    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    @Column(name = "diagnosis_completed", nullable = false)
    private boolean diagnosisCompleted;

    @Column(name = "refresh_token", length = 255)
    private String refreshToken;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    /** 앱 로그인/토큰 재발급 시 refreshToken을 갱신한다. */
    public void updateRefreshToken(String refreshToken, LocalDateTime refreshTokenExpiresAt) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

    /** 로그아웃/탈퇴 시 refreshToken을 무효화한다. */
    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiresAt = null;
    }

    /** 회원정보 수정: null인 값은 그대로 유지하고, 값이 있는 필드만 갱신한다. */
    public void updateProfile(String name, String email, String encodedPassword, String region, Boolean notificationEnabled) {
        if (name != null) {
            this.name = name;
        }
        if (email != null) {
            this.email = email;
        }
        if (encodedPassword != null) {
            this.password = encodedPassword;
        }
        if (region != null) {
            this.region = region;
        }
        if (notificationEnabled != null) {
            this.notificationEnabled = notificationEnabled;
        }
    }

    /** 앱 설치 유도 모달에서 [설치했어요]를 누른 경우. */
    public void markAppInstalled() {
        this.appInstalled = true;
    }

    /** 앱 설치 유도 모달에서 [나중에 할게요]를 누른 경우. 노출 횟수를 1 늘린다. */
    public void increaseInstallPromptCount() {
        this.installPromptCount += 1;
    }

    /** 비밀번호 재설정 링크 발송 시 토큰을 발급한다. */
    public void issueResetToken(String resetToken, LocalDateTime expiresAt) {
        this.resetToken = resetToken;
        this.resetTokenExpiresAt = expiresAt;
    }

    /** 비밀번호 재설정 완료 후 토큰을 1회용으로 소진 처리한다. */
    public void clearResetToken() {
        this.resetToken = null;
        this.resetTokenExpiresAt = null;
    }
}
