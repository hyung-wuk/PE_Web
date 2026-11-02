package com.umc.movie.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRatingRequest(
        @NotNull(message = "별점을 입력해주세요.") // [1]
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
        Integer score,
        @Size(max = 500, message = "후기는 500자 이하로 입력해주세요.") // [2]
        String comment
) {}

/*
 * [1] 이번 수정 API는 별점과 후기만 바꾸도록 설계했으므로 movieId는 받지 않는다.
 * 수정할 평점의 ratingId와 요청 회원의 memberId는 JSON이 아니라 URL 경로로 받는다.
 * PATCH 요청이어도 이 DTO에서는 score가 필수다. 모든 필드를 선택 입력으로 만든 방식은 아니다.
 * [2] comment를 생략하거나 null로 보내면 현재 구현은 기존 후기를 유지하지 않고 null로 변경한다.
 * 값이 있는 경우에는 500자 이하로 제한한다.
 */
