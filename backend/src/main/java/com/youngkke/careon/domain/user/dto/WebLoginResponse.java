package com.youngkke.careon.domain.user.dto;

/** POST /api/web/users/login 응답 body. */
public record WebLoginResponse(Integer userId, String accessToken, boolean diagnosisCompleted) {
}
