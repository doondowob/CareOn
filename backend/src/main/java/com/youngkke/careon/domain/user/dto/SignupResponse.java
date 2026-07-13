package com.youngkke.careon.domain.user.dto;

/** POST /api/web/users/register 응답 body (201 Created). */
public record SignupResponse(Integer userId, String accessToken, boolean diagnosisCompleted) {
}
