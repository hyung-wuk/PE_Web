// src/main/java/com/umc/movie/service/RatingService.java
package com.umc.movie.service;

import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.repository.RatingRepository;
import com.umc.movie.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true) // [1]
    public List<RatingResponse> getRatingsByMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) { // [2]
            // 공통 예외 처리기가 404 상태와 공통 응답 형식으로 변환합니다.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다.");
        }

        return ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(memberId).stream()
                .map(RatingResponse::from) // [3]
                .toList();
    }

    @Transactional
    public Long create(Long memberId, CreateRatingRequest request) { // [4]
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."));

        if (ratingRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new IllegalArgumentException("이미 이 영화에 평점을 남기셨습니다.");
        }

        Rating rating = Rating.builder()
                .member(member)
                .movieId(request.movieId())
                .score(request.score())
                .comment(request.comment())
                .build();

        return ratingRepository.save(rating).getRatingId();
    }

    @Transactional
    public void update(Long ratingId, Long memberId, UpdateRatingRequest request) { // [5]
        Rating rating = findOwnedRating(ratingId, memberId);
        rating.update(request.score(), request.comment());
        ratingRepository.save(rating);
    }

    @Transactional
    public void delete(Long ratingId, Long memberId) { // [6]
        Rating rating = findOwnedRating(ratingId, memberId);
        ratingRepository.delete(rating);
    }

    private Rating findOwnedRating(Long ratingId, Long memberId) { // [7]
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 평점입니다."));

        if (!rating.getMember().getMemberId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.");
        }

        return rating;
    }
}

/*
 * [1] @Transactional은 서비스의 DB 작업을 하나의 트랜잭션 범위로 묶는 설정이다.
 * Spring이 관리하는 서비스 메서드를 외부에서 호출할 때 프록시를 통해 적용된다.
 * 이 메서드에서는 회원 존재 확인 → 평점 조회 → 응답 DTO 변환을 같은 트랜잭션 범위에서 수행한다.
 * Hibernate의 영속성 컨텍스트도 이 범위에서 사용할 수 있어, 필요한 지연 로딩을 서비스 안에서 처리하기 좋다.
 * 프록시로 아직 읽지 않은 연관 데이터를 컨텍스트가 닫힌 뒤 읽으면 LazyInitializationException이 발생할 수 있다.
 *
 * readOnly=true는 "여기서는 조회만 할 것"이라는 설정이며, Hibernate 등의 읽기 최적화에 활용된다.
 * HTTP 메서드 설정이나 DB 계정의 쓰기 권한 차단이 아니므로, 쓰기를 절대 못 하게 만드는 보안 장치는 아니다.
 * 등록·수정·삭제 서비스는 보통 readOnly=true 없이 @Transactional로 쓰기 작업을 묶는다.
 * 쓰기 작업 중 롤백 대상 예외가 발생하면 같은 트랜잭션의 변경을 되돌려 일부만 저장되는 것을 방지한다.
 * Spring의 기본 롤백 대상은 RuntimeException과 Error이며, 모든 예외가 무조건 롤백되는 것은 아니다.
 *
 * 트랜잭션을 붙였다고 동시 요청이 한 줄로 처리되거나 중복 저장이 자동으로 방지되지는 않는다.
 * 중복 방지는 서비스 검증과 DB UNIQUE 제약 등을 함께 사용해야 한다.
 * 읽기 결과가 어떤 시점 기준으로 보이는지는 DB 격리 수준에도 영향을 받는다.
 *
 * [2] 회원 존재 여부를 확인하고 없으면 404를 던지는 이 if문이 서비스 검증이다.
 * @Transactional이 대신 회원을 검사하는 것이 아니라, 우리가 작성한 코드로 업무 규칙을 확인한다.
 *
 * [3] 조회한 Rating들을 트랜잭션 안에서 DTO로 변환한다.
 * 파생 쿼리로 받은 엔티티 리스트에서 하나씩 꺼내 DTO로 바꾸고, 응답 리스트에 모으는 흐름이다.
 * stream()은 리스트 항목을 처리할 Stream을 만들고, map()은 각 항목을 변환하며, toList()는 결과를 리스트로 모은다.
 * 이 세 메서드는 JpaRepository의 메서드가 아니라 Java의 컬렉션·Stream 기능이다.
 * 여기의 map()은 Map<String, Object>처럼 데이터를 담는 Map과는 다르다.
 *
 * RatingResponse::from은 메서드 참조 문법이며, 아래 람다와 같은 역할이다.
 * .map(rating -> RatingResponse.from(rating))
 * from은 RatingResponse에 우리가 직접 작성한 static 변환 메서드다. Java나 JPA의 특별한 기능이 아니다.
 * 각 Rating의 Getter로 값을 꺼내 새 RatingResponse 생성자에 넣어 반환한다.
 *
 * Stream 코드를 익숙한 for문으로 풀면 다음과 같다(아래 예시는 실행되지 않는 설명용 코드).
 *
 * List<Rating> ratings =
 *         ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(memberId);
 * List<RatingResponse> responses = new ArrayList<>();
 *
 * for (Rating rating : ratings) {
 *     RatingResponse response = RatingResponse.from(rating);
 *     responses.add(response);
 * }
 *
 * return responses;
 *
 * 즉 List<Rating> → 각 엔티티를 DTO로 변환 → List<RatingResponse> 반환이다.
 * for문 예시를 실제 코드로 쓰려면 Rating과 ArrayList의 import가 필요하다.
 * 처리 흐름은 같지만 Stream.toList()의 결과 리스트는 수정 불가능하고 ArrayList는 수정 가능하다는 차이가 있다.
 *
 * 현재 RatingResponse.from은 member의 닉네임 같은 연관 정보를 읽지 않으므로,
 * 이 코드가 LAZY 회원 조회를 실제로 실행하는 것은 아니다. 연관 필드에 접근할 때 지연 로딩이 필요할 수 있다.
 * 트랜잭션이 있어야 모든 단순 조회가 가능하다는 뜻이 아니라, 서비스 작업의 경계를 명확하게 둔 것이다.
 *
 * [4] create(): 컨트롤러가 URL에서 받은 memberId와 JSON으로 받은 요청 DTO를 전달한다.
 * findById()는 JpaRepository의 기본 조회 메서드로, 존재 여부만 확인하는 게 아니라 Optional<Member>를 반환한다.
 * orElseThrow()는 회원이 있으면 꺼내고, 없으면 람다로 만든 404 예외를 던져 아래 저장을 중단한다.
 * existsByMember_MemberIdAndMovieId()는 직접 선언한 파생 쿼리로, 같은 회원·같은 영화 조합의 중복을 확인한다.
 * 통과하면 빌더로 Rating 객체를 만들고, JpaRepository의 기본 메서드 save()로 저장한다.
 * 저장 후 DB가 자동 생성한 ratingId를 꺼내 반환하면 컨트롤러가 응답에 담는다.
 * 동시 요청의 중복까지 이 if문만으로 막을 수는 없으므로 DB의 복합 UNIQUE 제약도 필요하다.
 *
 * [5] update(): findOwnedRating()으로 평점 존재와 소유자를 확인한 뒤, 요청 DTO의 별점·후기를 전달한다.
 * rating.update()는 엔티티에 우리가 만든 메서드이고, ratingRepository.save()는 JPA가 제공하는 메서드다.
 * 전자는 객체 값 변경, 후자는 저장을 맡기는 역할이다.
 * 관리 중인 엔티티는 트랜잭션의 변경 감지로도 반영되지만, 여기서는 워크북대로 save() 호출을 유지한다.
 *
 * [6] delete(): 수정과 같은 소유자 검사 후 JpaRepository의 기본 메서드 delete()로 삭제한다.
 *
 * [7] findOwnedRating(): 수정·삭제에 반복되는 검사를 묶어 직접 만든 일반 메서드다.
 * ratingId로 평점을 조회해 없으면 404, 평점 작성자의 ID와 받은 memberId가 다르면 403 예외를 던진다.
 * 통과하면 찾아낸 Rating 객체를 반환한다. 회원 ID 조회가 아니라 평점 ID로 평점을 찾는 것에 주의한다.
 * 현재는 로그인 전 단계라 URL의 memberId를 신뢰하므로 실제 요청자의 신원까지 증명하지는 못한다.
 * 다른 사람의 memberId와 ratingId를 보내면 이 검사를 통과할 수 있다는 한계가 있다.
 * 이후 인증 도입 시에는 서버가 검증한 토큰의 회원 ID로 소유자를 비교해야 한다.
 */
