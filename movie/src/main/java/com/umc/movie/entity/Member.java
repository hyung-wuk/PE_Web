// src/main/java/com/umc/movie/entity/Member.java
package com.umc.movie.entity;

import jakarta.persistence.*; // [1]
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // [2]
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // [3]
    @Column(name = "member_id")
    private Long memberId;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    public Member(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }
}

/*
 * [1] jakarta.persistence: JPA의 어노테이션과 인터페이스 등이 들어 있는 패키지다.
 * @Entity, @Table, @Id, @Column 등을 여기서 가져다 쓴다.
 * import는 의존성을 설치하는 코드가 아니라, 이미 가져온 라이브러리의 타입을 사용하는 코드다.
 * JPA는 객체와 DB를 연결하는 표준 규칙이고, Hibernate는 그 규칙을 실제로 구현하는 라이브러리다.
 * spring-boot-starter-data-jpa 의존성을 추가하면 관련 라이브러리들을 함께 가져온다.
 *
 * [2] @NoArgsConstructor: 매개변수가 없는 생성자를 Lombok이 만들어 준다.
 * 여기서는 protected Member() {}를 작성한 것과 같다. 모든 필드에 값을 넣거나 의존성을 주입하는 기능이 아니다.
 * JPA가 DB에서 읽은 데이터를 Member 객체로 만들 때 사용할 기본 생성자를 준비하는 것이다.
 * PROTECTED는 외부에서 무분별하게 빈 객체를 만드는 것을 제한하면서 JPA가 사용할 수 있게 한다.
 * 우리가 새 회원을 만들 때는 아래에 직접 작성한 Member(email, password, nickname) 생성자를 사용한다.
 * 모든 필드를 받는 생성자는 @AllArgsConstructor이고,
 * 의존성 주입에 자주 쓰는 @RequiredArgsConstructor는 초기화되지 않은 final 필드 등을 받는 생성자를 만든다.
 *
 * [3] @GeneratedValue: PK 값을 자동으로 생성하는 방식을 지정한다.
 * IDENTITY는 MySQL의 AUTO_INCREMENT처럼 DB가 INSERT 시 ID를 생성하도록 맡기는 방식이다.
 * 따라서 회원 생성자에서 ID를 받지 않으며, 저장하면 JPA가 DB에서 생성된 ID를 엔티티에 반영한다.
 * ddl-auto=validate에서는 이 설정이 DB 컬럼을 변경하지 않는다. 기존 member_id도 AUTO_INCREMENT로 설정되어 있어야 한다.
 */
