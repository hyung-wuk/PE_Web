package com.umc.movie.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // [1]
public abstract class BaseTimeEntity {

    @CreationTimestamp // [2]
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // [3]
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

/*
 * [1] @MappedSuperclass: 공통 컬럼 매핑을 자식 엔티티에 물려준다.
 * 이 클래스 자체는 @Entity가 아니므로 대응하는 별도 테이블이 없다.
 * 예를 들어 Member가 extends BaseTimeEntity로 상속하면, 부모의 createdAt과 updatedAt도
 * member 테이블의 created_at과 updated_at에 매핑된다.
 * Rating이 상속하면 rating 테이블의 같은 이름 컬럼에 매핑된다.
 * 부모 테이블을 만들거나 FK로 연결해서 JOIN하는 것이 아니라 Java 상속으로 코드 중복을 줄이는 것이다.
 * ddl-auto=validate라면 자식 엔티티의 필드와 함께 이 컬럼들의 매핑도 검증 대상이 된다.
 *
 * [2] @CreationTimestamp: 엔티티를 처음 INSERT할 때 Hibernate가 생성 시간을 채운다.
 * 직접 생성자에서 시간을 넣지 않아도 된다.
 *
 * [3] @UpdateTimestamp: 처음 INSERT할 때와, 엔티티 변경으로 UPDATE할 때 Hibernate가 시간을 채운다.
 * 조회할 때마다 바뀌는 것이 아니며, 값이 그대로여서 UPDATE가 실행되지 않으면 갱신되지 않는다.
 */
