// src/main/java/com/umc/movie/service/MemberService.java
package com.umc.movie.service;

import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.SignUpRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional // [1]
    public MemberResponse signUp(SignUpRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) { // [2]
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 UTF-8 기준 72바이트 이하로 입력해주세요.");
        }
        if (memberRepository.existsByEmail(request.email())) { // [3]
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        Member member = Member.builder() // [4]
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // [5]
                .nickname(request.nickname())
                .build();
        Member savedMember = memberRepository.save(member); // [6]
        return new MemberResponse(savedMember.getMemberId(), savedMember.getEmail(), savedMember.getNickname()); // [7]
    }

    @Transactional(readOnly = true)
    public NicknameCheckResponse checkNickname(String nickname) {
        boolean exists = memberRepository.existsByNickname(nickname);
        return new NicknameCheckResponse(!exists);
    }

    @Transactional(readOnly = true)
    public EmailCheckResponse checkEmail(String email) {
        boolean exists = memberRepository.existsByEmail(email);
        return new EmailCheckResponse(!exists);
    }
}

/*
 * 회원가입 흐름: 요청값 확인 → 비밀번호 해시 → 회원 엔티티 생성 → 저장 → 응답 DTO 생성
 *
 * [1] @Transactional: 회원가입의 DB 작업을 하나의 트랜잭션으로 묶는다.
 * 메서드가 정상 종료된 뒤 트랜잭션이 커밋되어야 저장이 최종 확정된다.
 * 기본 설정에서 RuntimeException이나 Error가 전파되면 롤백된다.
 * 단, 트랜잭션으로 묶는다고 동시 가입 요청의 중복까지 자동으로 막히지는 않는다.
 * 서비스의 중복 확인과 별개로 DB의 UNIQUE 제약 조건도 필요하다.
 *
 * [2] request.password()는 요청 DTO에서 비밀번호 문자열을 꺼내는 접근 메서드다.
 * getBytes(StandardCharsets.UTF_8)는 문자열을 UTF-8 방식의 byte[]로 바꾼다.
 * 그 배열의 length는 문자 개수가 아니라 바이트 수다. UTF-8에서 영문 a는 1바이트, 한글 가는 3바이트다.
 * bcrypt의 입력 한도인 72바이트를 넘으면 400 예외를 던져 아래 과정을 진행하지 않는다.
 * 이 부분은 해시하는 코드가 아니라 해시하기 전 길이를 확인하는 보완 검사다.
 *
 * [3] existsByEmail과 아래의 existsByNickname은 레포지토리에 선언한 파생 쿼리다.
 * 요청 DTO에서 꺼낸 이메일·닉네임이 DB에 이미 있으면 true이므로 409 예외를 던진다.
 * 예외가 발생하면 아래의 객체 생성과 저장으로 넘어가지 않는다.
 *
 * [4] Member 생성자에 붙인 @Builder가 객체를 만드는 빌더 기능을 제공한다.
 * Member.builder()로 준비 도구를 만들고 email(), password(), nickname()으로 값을 준비한다.
 * 각 메서드는 빌더 자신을 반환하므로 점으로 이어서 호출할 수 있다.
 * 마지막 build()가 준비한 값들을 Member 생성자에 전달해 실제 회원 객체를 만든다.
 * 다음처럼 생성자를 직접 호출하는 것과 같은 방식으로 값을 담는다.
 *
 * String hashedPassword = passwordEncoder.encode(request.password());
 * Member member = new Member(request.email(), hashedPassword, request.nickname());
 *
 * 빌더는 어느 값이 어느 항목인지 이름으로 보여주는 방식이다. build()만으로 DB에 저장되지는 않는다.
 *
 * [5] 안쪽의 passwordEncoder.encode(request.password())가 먼저 실행되어 해시 문자열을 반환한다.
 * 바깥의 빌더 password(...)는 그 결과를 받아 준비한다. password(...) 자체가 해시하는 것은 아니다.
 * 따라서 Member 객체의 password에는 평문 대신 해시된 값이 들어간다.
 *
 * [6] save()는 직접 작성한 파생 쿼리가 아니라 JpaRepository를 상속하여 사용하는 기본 저장 메서드다.
 * 여기서는 ID가 없는 새 회원이므로 JPA가 새 엔티티로 판단해 persist()로 저장을 진행한다.
 * 일반적으로 save()는 새 엔티티에는 persist(), 기존 엔티티에는 merge()를 사용한다.
 * DB의 AUTO_INCREMENT로 만들어진 회원 ID가 엔티티에 반영되어 반환 결과에서 꺼낼 수 있다.
 * savedMember는 저장 결과 엔티티를 받는 변수이며, 새 DTO를 받거나 DB를 다시 조회한다는 뜻은 아니다.
 * save()가 반환되어도 트랜잭션의 최종 커밋 전에는 저장이 확정된 것이 아니다.
 *
 * [7] 저장 결과에서 회원 ID·이메일·닉네임만 꺼내 MemberResponse 생성자에 넣어 반환한다.
 * 비밀번호는 해시된 값이라도 응답에 포함하지 않는다.
 * 서비스는 응답 DTO를 반환하고, 컨트롤러가 이를 ApiResponse로 감싸 HTTP 응답을 만든다.
 */
