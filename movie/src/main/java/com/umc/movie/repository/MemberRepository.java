// src/main/java/com/umc/movie/repository/MemberRepository.java
package com.umc.movie.repository;

import com.umc.movie.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByNickname(String nickname); // [1]
    boolean existsByEmail(String email); // [2]
}

/*
 * 메서드 이름으로 조건을 표현하는 쿼리 메서드(파생 쿼리)다.
 * Spring Data JPA가 이름을 해석해 조회를 구성하므로 우리가 구현 클래스나 SQL을 직접 작성하지 않는다.
 * 이름의 기준은 DB 컬럼명이 아니라 엔티티의 Java 필드명이다.
 *
 * [1] existsByNickname: "전달한 nickname과 같은 회원이 존재하는가?"
 * exists는 존재 여부 확인, By 뒤의 Nickname은 Member.nickname을 조건으로 사용한다는 뜻이다.
 * nickname 인자의 값이 조건에 들어가며, 회원이 있으면 true, 없으면 false를 반환한다.
 * 이 결과는 중복 여부다. API의 available(사용 가능 여부)을 만들 때는 !결과로 반대로 바꾼다.
 *
 * [2] existsByEmail: "전달한 email과 같은 회원이 존재하는가?"
 * [1]과 같은 방식으로 Member.email을 조건으로 사용한다.
 * 같은 이메일이 있으면 true(중복), 없으면 false(사용 가능)를 반환한다.
 */
