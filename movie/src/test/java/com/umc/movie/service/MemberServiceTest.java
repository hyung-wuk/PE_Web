package com.umc.movie.service;

import com.umc.movie.config.PasswordConfig;
import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.SignUpRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder = new PasswordConfig().passwordEncoder();
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, passwordEncoder);
    }

    @Test
    void signUpSavesHashAndReturnsPublicFields() {
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "memberId", 1L);
            return member;
        });

        MemberResponse response = memberService.signUp(validRequest());

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).hasSize(60).isNotEqualTo(validRequest().password());
        assertThat(passwordEncoder.matches(validRequest().password(), captor.getValue().getPassword())).isTrue();
        assertThat(response).isEqualTo(new MemberResponse(1L, "umc@example.com", "moviefan"));
    }

    @Test
    void duplicateEmailStopsSaving() {
        given(memberRepository.existsByEmail("umc@example.com")).willReturn(true);

        assertThatThrownBy(() -> memberService.signUp(validRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
        verify(memberRepository, never()).save(any());
    }

    @Test
    void duplicateNicknameStopsSaving() {
        given(memberRepository.existsByNickname("moviefan")).willReturn(true);

        assertThatThrownBy(() -> memberService.signUp(validRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 닉네임입니다.");
        verify(memberRepository, never()).save(any());
    }

    @Test
    void oversizedUtf8PasswordIsRejectedBeforeHashing() {
        SignUpRequest request = new SignUpRequest("umc@example.com", "Aa1!" + "가".repeat(24), "moviefan");

        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verifyNoInteractions(memberRepository);
    }

    @Test
    void databaseConstraintCanStillRejectAfterPrechecks() {
        given(memberRepository.save(any(Member.class)))
                .willThrow(new DataIntegrityViolationException("concurrent duplicate"));

        assertThatThrownBy(() -> memberService.signUp(validRequest()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private SignUpRequest validRequest() {
        return new SignUpRequest("umc@example.com", "Example1!", "moviefan");
    }
}
