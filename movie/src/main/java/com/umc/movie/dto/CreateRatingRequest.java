package com.umc.movie.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateRatingRequest(
        @NotNull(message = "영화 ID를 입력해주세요.") // [1]
        @Positive(message = "영화 ID는 양수여야 합니다.")
        Long movieId,
        @NotNull(message = "별점을 입력해주세요.") // [2]
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
        Integer score,
        @Size(max = 500, message = "후기는 500자 이하로 입력해주세요.") // [3]
        String comment
) {}

/*
 * [1] movieId는 TMDB 영화 번호다. Long과 @Positive로 양의 정수를 받고 @NotNull로 누락을 막는다.
 * 우리 DB의 Movie 엔티티를 연결하는 FK가 아니며, 양수 검사만으로 TMDB에 실제 존재하는지는 확인하지 않는다.
 * 평점 자체의 ratingId는 DB의 AUTO_INCREMENT로 생성하므로 등록 요청에서 받지 않는다.
 * [2] score는 필수이며 1~5점만 허용한다. 컨트롤러의 @Valid가 이 규칙을 검사한다.
 * [3] comment는 선택 입력이다. @Size는 null을 허용하지만 값이 있으면 500자 이하인지 검사한다.
 */
