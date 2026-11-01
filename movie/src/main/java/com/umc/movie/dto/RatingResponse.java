// src/main/java/com/umc/movie/dto/RatingResponse.java
package com.umc.movie.dto;

import com.umc.movie.entity.Rating;

import java.time.LocalDateTime;

public record RatingResponse(
        Long ratingId,
        Long movieId,
        Integer score,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RatingResponse from(Rating rating) { // [1]
        return new RatingResponse( // [2]
                rating.getRatingId(),
                rating.getMovieId(),
                rating.getScore(),
                rating.getComment(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}

/*
 * [1] from은 Rating 엔티티의 값을 RatingResponse DTO로 옮겨 담는 메서드다.
 * Java의 특별 문법이 아니라 "이 엔티티로부터 DTO를 만든다"는 의미로 붙인 이름이다.
 * static이므로 DTO 객체를 먼저 만들지 않고 RatingResponse.from(rating)으로 호출한다.
 * 예전에 서비스에서 new StudyResponse(study.getId(), ...)로 직접 변환하던 코드를
 * 여기로 옮겨 한 메서드로 묶은 것이다. 의존성 주입이나 자동 변환 기능은 아니다.
 * 서비스에서 직접 생성해도 되지만, 이 메서드를 사용하면 변환 코드를 반복해서 쓰지 않아도 된다.
 *
 * [2] 함수를 인자로 전달하는 것이 아니라, Getter를 실행해서 얻은 값을 생성자에 전달한다.
 * rating.getScore()가 5를 반환하면 생성자에 들어가는 것은 메서드가 아니라 숫자 5다.
 * record가 자동으로 제공하는 생성자는 위에 선언한 필드 순서대로 값을 받는다.
 * return new RatingResponse(...)는 "DTO 객체를 만들어 바로 반환한다"는 뜻이다.
 * 중간 변수를 생략한 표현이며, 길게 풀면 아래 코드와 같다.
 *
 * public static RatingResponse from(Rating rating) {
 *     Long ratingId = rating.getRatingId();
 *     Long movieId = rating.getMovieId();
 *     Integer score = rating.getScore();
 *     String comment = rating.getComment();
 *     LocalDateTime createdAt = rating.getCreatedAt();
 *     LocalDateTime updatedAt = rating.getUpdatedAt();
 *
 *     RatingResponse response = new RatingResponse(
 *             ratingId, movieId, score, comment, createdAt, updatedAt
 *     );
 *     return response;
 * }
 *
 * 호출하는 서비스에서는 다음 한 줄로 DTO를 만들 수 있다.
 * RatingResponse response = RatingResponse.from(rating);
 */
