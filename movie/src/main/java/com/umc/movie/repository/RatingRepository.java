// src/main/java/com/umc/movie/repository/RatingRepository.java
package com.umc.movie.repository;

import com.umc.movie.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByMember_MemberIdOrderByRatingIdDesc(Long memberId); // [1]
}

/*
 * [1] findByMember_MemberIdOrderByRatingIdDesc: 특정 회원의 평점 목록을 평점 ID 내림차순으로 조회한다.
 * 길지만 "조회 동작 + 조건 + 정렬"로 나누어 읽으면 된다.
 *
 * findBy: 조건에 맞는 엔티티를 조회한다. 여기서는 반환 타입이 List<Rating>이므로 여러 평점을 받는다.
 * Member_MemberId: Rating.member를 따라가서 그 안의 Member.memberId를 조건으로 사용한다.
 * 즉 rating.member.memberId가 인자로 받은 memberId와 같은 평점들을 찾는다.
 * 밑줄은 객체 속성의 탐색 경계를 명확하게 나눈 것이다. DB의 member_id를 그대로 적은 것이 아니다.
 * OrderByRatingIdDesc: Rating.ratingId를 기준으로 내림차순 정렬한다. 큰 ID가 앞에 온다.
 *
 * SQL로 생각하면 다음 조건과 정렬에 해당한다(실제 생성 SQL의 모양은 달라질 수 있다).
 * SELECT * FROM rating WHERE member_id = ? ORDER BY rating_id DESC;
 * ?에는 호출할 때 전달한 memberId가 들어간다.
 *
 * 결과는 Rating 엔티티 목록이고 응답 DTO나 JSON은 아니다. 일치하는 평점이 없으면 빈 리스트를 반환한다.
 * 이 조회만으로는 회원 자체가 없는지, 회원은 있지만 평점이 없는지 구분하지 못한다.
 * 또한 ID 내림차순은 생성 시간 내림차순 자체가 아니다. 생성 시간 기준이 필요하면 그 필드를 기준으로 정렬한다.
 */
