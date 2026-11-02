// src/main/java/com/umc/movie/controller/RatingController.java
package com.umc.movie.controller;

import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class RatingController {

    private final RatingService ratingService;

    @GetMapping("/{memberId}/ratings")
    public ApiResponse<List<RatingResponse>> getMemberRatings(@PathVariable Long memberId) {
        return ApiResponse.ok("평점 목록 조회에 성공했습니다.",
                ratingService.getRatingsByMember(memberId));
    }

    @PostMapping("/{memberId}/ratings") // [1]
    public ResponseEntity<ApiResponse<Long>> create(
            @PathVariable Long memberId,
            @Valid @RequestBody CreateRatingRequest request
    ) {
        Long ratingId = ratingService.create(memberId, request);
        return ResponseEntity.status(201).body(ApiResponse.created("평점 등록에 성공했습니다.", ratingId));
    }

    @PatchMapping("/{memberId}/ratings/{ratingId}") // [2]
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long memberId,
            @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingRequest request
    ) {
        ratingService.update(ratingId, memberId, request);
        return ResponseEntity.ok(ApiResponse.ok("평점 수정에 성공했습니다.", null));
    }

    @DeleteMapping("/{memberId}/ratings/{ratingId}") // [3]
    public ResponseEntity<Void> delete(@PathVariable Long memberId, @PathVariable Long ratingId) {
        ratingService.delete(ratingId, memberId);
        return ResponseEntity.noContent().build();
    }
}

/*
 * 공통: @PathVariable은 URL의 ID, @RequestBody는 JSON을 DTO로 받고 @Valid는 DTO의 검증 규칙을 실행한다.
 * 검증을 통과하면 서비스에 전달한다. 예외가 발생하면 전역 핸들러가 처리하고, 정상 종료되면 성공 응답을 만든다.
 * [1] 등록: 회원 ID와 등록 DTO를 서비스에 전달하고, 생성된 평점 ID를 받아 HTTP 201과 ApiResponse로 반환한다.
 * [2] 수정: 두 ID와 수정 DTO를 서비스에 전달한다. 반환할 데이터가 없어 HTTP 200과 result=null인 성공 본문을 보낸다.
 * [3] 삭제: 두 ID만 서비스에 전달하며 요청 DTO는 없다. HTTP 204는 본문 없이 반환하므로 ApiResponse로 감싸지 않는다.
 */
