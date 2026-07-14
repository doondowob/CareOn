package com.youngkke.careon.domain.user;

import com.youngkke.careon.domain.notification.NotificationRepository;
import com.youngkke.careon.domain.policy.InterestPolicyType;
import com.youngkke.careon.domain.policy.InterestPolicyTypeRepository;
import com.youngkke.careon.domain.policy.PolicyType;
import com.youngkke.careon.domain.policy.PolicyTypeRepository;
import com.youngkke.careon.domain.policy.SavedPolicy;
import com.youngkke.careon.domain.policy.SavedPolicyRepository;
import com.youngkke.careon.domain.todo.TodoRepository;
import com.youngkke.careon.domain.user.dto.AppInstallStatusRequest;
import com.youngkke.careon.domain.user.dto.AppLoginResponse;
import com.youngkke.careon.domain.user.dto.AppMeResponse;
import com.youngkke.careon.domain.user.dto.LoginRequest;
import com.youngkke.careon.domain.user.dto.PasswordResetLinkRequest;
import com.youngkke.careon.domain.user.dto.PasswordResetRequest;
import com.youngkke.careon.domain.user.dto.RefreshTokenRequest;
import com.youngkke.careon.domain.user.dto.RefreshTokenResponse;
import com.youngkke.careon.domain.user.dto.SignupRequest;
import com.youngkke.careon.domain.user.dto.SignupResponse;
import com.youngkke.careon.domain.user.dto.UpdateAppProfileRequest;
import com.youngkke.careon.domain.user.dto.UpdateWebProfileRequest;
import com.youngkke.careon.domain.user.dto.WebLoginResponse;
import com.youngkke.careon.domain.user.dto.WebMeResponse;
import com.youngkke.careon.global.auth.JwtProvider;
import com.youngkke.careon.global.dto.MessageResponse;
import com.youngkke.careon.global.error.BusinessException;
import com.youngkke.careon.global.error.ErrorCode;
import com.youngkke.careon.global.mail.MailService;
import io.jsonwebtoken.JwtException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PolicyTypeRepository policyTypeRepository;
    private final InterestPolicyTypeRepository interestPolicyTypeRepository;
    private final SavedPolicyRepository savedPolicyRepository;
    private final TodoRepository todoRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final MailService mailService;

    private static final int RESET_TOKEN_VALIDITY_MINUTES = 30;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        List<PolicyType> policyTypes = request.interestPolicyTypeIds().stream()
                .map(id -> policyTypeRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(ErrorCode.POLICY_TYPE_NOT_FOUND)))
                .toList();

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .region(request.region())
                .termsAgreed(true)
                .installPromptCount(0)
                .appInstalled(false)
                .notificationEnabled(true)
                .diagnosisCompleted(false)
                .build();
        userRepository.save(user);

        for (PolicyType policyType : policyTypes) {
            interestPolicyTypeRepository.save(InterestPolicyType.builder()
                    .user(user)
                    .policyType(policyType)
                    .build());
        }

        String accessToken =
                jwtProvider.createAccessToken(user.getUserId(), jwtProvider.getWebAccessTokenValiditySeconds());

        return new SignupResponse(user.getUserId(), accessToken, user.isDiagnosisCompleted());
    }

    public WebLoginResponse loginWeb(LoginRequest request) {
        User user = authenticate(request);
        String accessToken =
                jwtProvider.createAccessToken(user.getUserId(), jwtProvider.getWebAccessTokenValiditySeconds());
        return new WebLoginResponse(user.getUserId(), accessToken, user.isDiagnosisCompleted());
    }

    @Transactional
    public AppLoginResponse loginApp(LoginRequest request) {
        User user = authenticate(request);
        String accessToken =
                jwtProvider.createAccessToken(user.getUserId(), jwtProvider.getAppAccessTokenValiditySeconds());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());
        user.updateRefreshToken(refreshToken, toKstLocalDateTime(jwtProvider.getRefreshTokenExpiry()));
        return new AppLoginResponse(user.getUserId(), accessToken, refreshToken);
    }

    @Transactional
    public MessageResponse logout(Integer userId) {
        User user = getUserOrThrow(userId);
        user.clearRefreshToken();
        return new MessageResponse("로그아웃되었습니다.");
    }

    @Transactional
    public RefreshTokenResponse reissueToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        Integer userId;
        try {
            if (!jwtProvider.isRefreshToken(refreshToken)) {
                throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
            }
            userId = jwtProvider.getUserId(refreshToken);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        // 기존에 발급했던 refreshToken과 다르면(이미 재발급되어 교체됐거나 위조된 경우) 거부한다.
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        String newAccessToken =
                jwtProvider.createAccessToken(userId, jwtProvider.getAppAccessTokenValiditySeconds());
        String newRefreshToken = jwtProvider.createRefreshToken(userId);
        user.updateRefreshToken(newRefreshToken, toKstLocalDateTime(jwtProvider.getRefreshTokenExpiry()));

        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    public WebMeResponse getWebMe(Integer userId) {
        User user = getUserOrThrow(userId);
        return new WebMeResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRegion(),
                user.isDiagnosisCompleted(),
                user.isAppInstalled(),
                user.getInstallPromptCount());
    }

    public AppMeResponse getAppMe(Integer userId) {
        User user = getUserOrThrow(userId);
        return new AppMeResponse(
                user.getUserId(), user.getName(), user.getEmail(), user.getRegion(), user.isNotificationEnabled());
    }

    @Transactional
    public MessageResponse updateWebProfile(Integer userId, UpdateWebProfileRequest request) {
        User user = getUserOrThrow(userId);
        checkEmailDuplicate(user, request.email());
        String encodedPassword = encodeIfPresent(request.password());
        user.updateProfile(request.name(), request.email(), encodedPassword, request.region(), null);
        return new MessageResponse("회원 정보가 수정되었습니다.");
    }

    @Transactional
    public MessageResponse updateAppProfile(Integer userId, UpdateAppProfileRequest request) {
        User user = getUserOrThrow(userId);
        checkEmailDuplicate(user, request.email());
        String encodedPassword = encodeIfPresent(request.password());
        user.updateProfile(
                request.name(), request.email(), encodedPassword, request.region(), request.notificationEnabled());
        return new MessageResponse("회원 정보가 수정되었습니다.");
    }

    @Transactional
    public MessageResponse updateAppInstallStatus(Integer userId, AppInstallStatusRequest request) {
        User user = getUserOrThrow(userId);
        if (Boolean.TRUE.equals(request.installed())) {
            user.markAppInstalled();
        } else {
            user.increaseInstallPromptCount();
        }
        return new MessageResponse("처리되었습니다.");
    }

    /** 회원 탈퇴(웹/앱 공통). 연관된 saved_policies/todos/notifications/interest_policy_types를 먼저 정리한 뒤 유저를 삭제한다. */
    @Transactional
    public MessageResponse withdraw(Integer userId) {
        User user = getUserOrThrow(userId);

        List<SavedPolicy> savedPolicies = savedPolicyRepository.findAllByUser(user);
        if (!savedPolicies.isEmpty()) {
            notificationRepository.deleteAllBySavedPolicyIn(savedPolicies);
            todoRepository.deleteAllBySavedPolicyIn(savedPolicies);
            savedPolicyRepository.deleteAllByUser(user);
        }
        interestPolicyTypeRepository.deleteAllByUser(user);
        userRepository.delete(user);

        return new MessageResponse("회원 탈퇴가 완료되었습니다.");
    }

    /**
     * 비밀번호 재설정 링크 발송. 계정 존재 여부를 노출하지 않기 위해, 가입 여부와 무관하게 항상 같은 응답을 준다.
     * 실제 이메일 발송/토큰 발급은 계정이 존재할 때만 내부적으로 수행한다.
     */
    @Transactional
    public MessageResponse sendPasswordResetLink(PasswordResetLinkRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString().replace("-", "");
            user.issueResetToken(resetToken, LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(RESET_TOKEN_VALIDITY_MINUTES));
            mailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        });
        return new MessageResponse("비밀번호 재설정을 위해 이메일을 확인해보세요.");
    }

    @Transactional
    public MessageResponse resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByResetToken(request.resetToken())
                .filter(u -> u.getResetTokenExpiresAt() != null
                        && u.getResetTokenExpiresAt().isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul"))))
                .orElseThrow(() -> new BusinessException(ErrorCode.RESET_LINK_INVALID));

        user.updateProfile(null, null, passwordEncoder.encode(request.newPassword()), null, null);
        user.clearResetToken();

        return new MessageResponse("비밀번호가 재설정되었습니다.");
    }

    private void checkEmailDuplicate(User user, String newEmail) {
        if (newEmail != null && !newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    private String encodeIfPresent(String rawPassword) {
        return rawPassword != null ? passwordEncoder.encode(rawPassword) : null;
    }

    private User getUserOrThrow(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    private User authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        return user;
    }

    private LocalDateTime toKstLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"));
    }
}
