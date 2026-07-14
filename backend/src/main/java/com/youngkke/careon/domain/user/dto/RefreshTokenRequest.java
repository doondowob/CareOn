package com.youngkke.careon.domain.user.dto;

/** POST /api/app/users/refresh 요청 body. 유효성 검증은 서비스 레이어에서 401로 통일해서 처리한다. */
public record RefreshTokenRequest(String refreshToken) {
}
