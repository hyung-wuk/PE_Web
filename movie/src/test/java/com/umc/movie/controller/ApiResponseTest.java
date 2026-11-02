package com.umc.movie.controller;

import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.SignUpRequest;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.service.MemberService;
import com.umc.movie.service.RatingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.anyOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({MemberController.class, RatingController.class})
class ApiResponseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private RatingService ratingService;

    @Test
    void createRatingReturns201AndId() throws Exception {
        given(ratingService.create(1L, new CreateRatingRequest(550L, 5, null))).willReturn(10L);
        mockMvc.perform(post("/members/1/ratings").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movieId":550,"score":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result").value(10));
    }

    @Test
    void invalidRatingInputDoesNotReachService() throws Exception {
        mockMvc.perform(post("/members/1/ratings").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movieId":0,"score":6}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON4001"));
        verify(ratingService, never()).create(any(), any());

        mockMvc.perform(patch("/members/1/ratings/10").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verify(ratingService, never()).update(any(), any(), any());
    }

    @Test
    void updateRatingReturns200AndNullResult() throws Exception {
        mockMvc.perform(patch("/members/1/ratings/10").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"score":3,"comment":"수정"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result").value(nullValue()));
        verify(ratingService).update(10L, 1L, new UpdateRatingRequest(3, "수정"));
    }

    @Test
    void deleteRatingReturns204WithoutBody() throws Exception {
        mockMvc.perform(delete("/members/1/ratings/10"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(ratingService).delete(10L, 1L);
    }

    @Test
    void signUpReturns201WithoutPassword() throws Exception {
        given(memberService.signUp(any(SignUpRequest.class)))
                .willReturn(new MemberResponse(1L, "umc@example.com", "moviefan"));

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"umc@example.com","password":"Example1!","nickname":"moviefan"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.memberId").value(1))
                .andExpect(jsonPath("$.result.email").value("umc@example.com"))
                .andExpect(jsonPath("$.result.nickname").value("moviefan"))
                .andExpect(jsonPath("$.result.password").doesNotExist());
    }

    @Test
    void invalidSignUpIsRejectedBeforeServiceAndDoesNotEchoPassword() throws Exception {
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"invalid-email","password":"weak","nickname":"x"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON4001"))
                .andExpect(jsonPath("$.message").value(anyOf(
                        containsString("올바른 이메일"), containsString("닉네임은 2자"), containsString("비밀번호는"))))
                .andExpect(content().string(not(containsString("weak"))))
                .andExpect(jsonPath("$.result").value(nullValue()));
        verify(memberService, never()).signUp(any());
    }

    @Test
    void missingSignUpFieldsAreRejected() throws Exception {
        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false));
        verify(memberService, never()).signUp(any());
    }

    @Test
    void duplicateSignUpReturns409() throws Exception {
        given(memberService.signUp(any(SignUpRequest.class)))
                .willThrow(new IllegalArgumentException("이미 사용 중인 이메일입니다."));

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"umc@example.com","password":"Example1!","nickname":"moviefan"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    void nicknameCheckWrapsExistingDto() throws Exception {
        given(memberService.checkNickname("noco")).willReturn(new NicknameCheckResponse(true));

        mockMvc.perform(get("/members/nickname/noco")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("닉네임 중복 확인에 성공했습니다."))
                .andExpect(jsonPath("$.result.available").value(true))
                .andExpect(jsonPath("$.available").doesNotExist());
    }

    @Test
    void duplicateEmailIsStillSuccessfulAvailabilityCheck() throws Exception {
        given(memberService.checkEmail("noco@example.com")).willReturn(new EmailCheckResponse(false));

        mockMvc.perform(get("/members/email/noco@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.available").value(false));
    }

    @Test
    void ratingsAreReturnedInsideResult() throws Exception {
        LocalDateTime time = LocalDateTime.of(2026, 10, 28, 10, 0);
        given(ratingService.getRatingsByMember(1L))
                .willReturn(List.of(new RatingResponse(1L, 550L, 5, "좋아요", time, time)));

        mockMvc.perform(get("/members/1/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result[0].ratingId").value(1))
                .andExpect(jsonPath("$.result[0].movieId").value(550))
                .andExpect(jsonPath("$.result[0].score").value(5));
    }

    @Test
    void emptyRatingsRemainEmptyArray() throws Exception {
        given(ratingService.getRatingsByMember(1L)).willReturn(List.of());

        mockMvc.perform(get("/members/1/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void missingMemberKeeps404AndUsesCommonFailure() throws Exception {
        given(ratingService.getRatingsByMember(99L)).willThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."));

        mockMvc.perform(get("/members/99/ratings"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON404"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 유저입니다."))
                .andExpect(jsonPath("$.result").value(nullValue()));
    }

    @Test
    void invalidPathTypeFollowsWorkbookIllegalArgumentHandler() throws Exception {
        mockMvc.perform(get("/members/not-a-number/ratings"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COMMON4092"));
    }

    @Test
    void unsupportedMethodKeeps405() throws Exception {
        mockMvc.perform(post("/members/nickname/noco"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void constraintViolationDoesNotExposeDatabaseDetails() throws Exception {
        given(memberService.checkNickname("noco"))
                .willThrow(new DataIntegrityViolationException("internal SQL details"));

        mockMvc.perform(get("/members/nickname/noco"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON4091"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 데이터입니다."));
    }

    @Test
    void illegalArgumentUsesCommon4092() throws Exception {
        given(memberService.signUp(any(SignUpRequest.class)))
                .willThrow(new IllegalArgumentException("이미 사용 중인 이메일입니다."));

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"umc@example.com","password":"Example1!","nickname":"moviefan"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON4092"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."))
                .andExpect(jsonPath("$.result").value(nullValue()));
    }

}
