// src/main/java/com/umc/movie/entity/Rating.java
package com.umc.movie.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rating", uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "movie_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rating extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private Long ratingId;

    @ManyToOne(fetch = FetchType.LAZY) // [1]
    @JoinColumn(name = "member_id", nullable = false) // [2]
    private Member member;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Builder // [3]
    public Rating(Member member, Long movieId, Integer score, String comment) {
        this.member = member;
        this.movieId = movieId;
        this.score = score;
        this.comment = comment;
    }

    public void update(Integer score, String comment) { // [4]
        this.score = score;
        this.comment = comment;
    }
}

/*
 * [1] Rating에서 Member를 바라보는 관계는 다대일(ManyToOne)이다.
 * 한 회원은 여러 평점을 작성할 수 있지만, 평점 하나의 작성자는 회원 한 명이기 때문이다.
 * 회원 쪽에서 보면 일대다지만, 이 필드는 평점 쪽이므로 다대일로 표현한다.
 *
 * fetch=LAZY는 평점을 조회할 때 연결된 회원의 모든 정보까지 무조건 가져오지 않도록 하는 지연 로딩 설정이다.
 * Hibernate는 보통 회원 객체 자리에 프록시를 두고, 회원 정보가 실제로 필요할 때 조회한다.
 * 예: rating.getMember().getNickname()으로 닉네임을 읽으면 회원 조회가 추가로 실행될 수 있다.
 * getMember()를 호출하기만 했다고 언제나 즉시 조회하는 것은 아니며, 이미 읽은 회원이면 다시 조회하지 않을 수 있다.
 * 필요 없는 회원 조회를 줄이는 목적이고, LAZY 자체가 SQL JOIN을 뜻하거나 FK를 만드는 것은 아니다.
 * 여러 평점의 회원 정보를 반복해서 읽으면 추가 조회가 많아지는 N+1 문제가 생길 수 있다.
 * 회원 정보가 함께 필요한 조회에서는 fetch join이나 EntityGraph 같은 방법을 별도로 고려한다.
 *
 * [2] Java에서는 Member 객체를 연결하지만, DB의 rating.member_id 컬럼에는 회원의 PK 값이 저장된다.
 * name은 이 관계에 사용할 실제 FK 컬럼 이름이며, nullable=false는 회원 없는 평점을 허용하지 않는 매핑이다.
 * ddl-auto=validate에서는 이 선언이 기존 DB에 FK 제약을 새로 만들어주지는 않는다. DB 제약은 별도로 있어야 한다.
 * 지연 로딩으로 아직 읽지 않은 회원 정보는 보통 영속성 컨텍스트가 열려 있을 때 접근해야 한다.
 * 서비스의 트랜잭션 안에서 필요한 데이터를 읽고 DTO로 바꾸면, 응답 단계에서 뒤늦게 지연 조회하는 상황을 줄일 수 있다.
 * DB에서 rating.member_id를 보고 member의 같은 PK 행을 따라가는 관계를 Java에서는 객체 참조로 표현한 것이다.
 * member는 필드 이름이고 Member는 타입이며, 이 필드는 회원 객체를 참조한다. DB에는 객체 전체가 아니라 FK 값이 저장된다.
 * JoinColumn은 현재 rating 테이블의 FK 컬럼을 지정한다. 회원의 어떤 필드만 조회할지 고르는 설정이 아니다.
 * LAZY도 회원 필드별 선택 조회가 아니라 연결된 회원 객체의 실제 조회 시점을 미루는 설정이다.
 *
 * [3] 생성자는 새 평점에 작성자 객체·영화 번호·별점·후기를 넣는다.
 * @Builder는 이 생성자를 이용하는 빌더를 제공하며, 마지막 build()가 준비한 값으로 객체를 만든다.
 * new Rating(member, movieId, score, comment)로 직접 생성하는 것과 같은 목적이다.
 * SecLog의 Study에서는 생성자를 직접 호출했고, 여기서는 빌더 방식으로 값을 전달한다.
 * 생성자에 ratingId가 없는 이유는 DB가 AUTO_INCREMENT로 생성하기 때문이다. build()만으로 DB에 저장되지는 않는다.
 *
 * [4] update()는 JPA가 제공하는 메서드가 아니라 우리가 엔티티에 직접 만든 일반 메서드다.
 * this.score와 this.comment는 기존 객체의 필드이고, 오른쪽 값은 호출할 때 전달한 매개변수다.
 * 새 객체를 만들거나 작성자·영화 번호를 바꾸지 않고 기존 평점의 별점과 후기만 변경한다.
 * 객체 값을 바꾸는 동작 자체는 DB 저장 명령이 아니다. 서비스의 트랜잭션에서 DB 반영이 진행된다.
 * SecLog의 put(), patch()처럼 이름을 직접 정한 것이며 이름만으로 HTTP 메서드 기능이 생기지는 않는다.
 */
