package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {
    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private MemberRepository memberRepository;

    private RatingService service;
    private Member owner;

    @BeforeEach
    void setUp() {
        service = new RatingService(ratingRepository, memberRepository);
        owner = new Member("owner@example.com", "hash", "owner");
        ReflectionTestUtils.setField(owner, "memberId", 1L);
    }

    @Test
    void createSavesMemberAndExternalMovieId() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(owner));
        given(ratingRepository.save(any(Rating.class))).willAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            ReflectionTestUtils.setField(rating, "ratingId", 10L);
            return rating;
        });

        assertThat(service.create(1L, new CreateRatingRequest(550L, 5, null))).isEqualTo(10L);
        ArgumentCaptor<Rating> captor = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).save(captor.capture());
        assertThat(captor.getValue().getMember()).isSameAs(owner);
        assertThat(captor.getValue().getMovieId()).isEqualTo(550L);
        assertThat(captor.getValue().getScore()).isEqualTo(5);
        assertThat(captor.getValue().getComment()).isNull();
    }

    @Test
    void createRejectsMissingMember() {
        assertThatThrownBy(() -> service.create(99L, new CreateRatingRequest(550L, 5, null)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateBeforeSaving() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(owner));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);
        assertThatThrownBy(() -> service.create(1L, new CreateRatingRequest(550L, 5, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 이 영화에 평점을 남기셨습니다.");
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void ownerCanUpdate() {
        Rating rating = existingRating();
        given(ratingRepository.findById(10L)).willReturn(Optional.of(rating));
        service.update(10L, 1L, new UpdateRatingRequest(3, "수정"));
        assertThat(rating.getScore()).isEqualTo(3);
        assertThat(rating.getComment()).isEqualTo("수정");
        verify(ratingRepository).save(rating);
    }

    @Test
    void ownerCanDelete() {
        Rating rating = existingRating();
        given(ratingRepository.findById(10L)).willReturn(Optional.of(rating));
        service.delete(10L, 1L);
        verify(ratingRepository).delete(rating);
    }

    @Test
    void nonOwnerCannotUpdateOrDelete() {
        Rating rating = existingRating();
        given(ratingRepository.findById(10L)).willReturn(Optional.of(rating));
        assertThatThrownBy(() -> service.update(10L, 2L, new UpdateRatingRequest(3, null)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
        assertThatThrownBy(() -> service.delete(10L, 2L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
        assertThat(rating.getScore()).isEqualTo(5);
        verify(ratingRepository, never()).save(any());
        verify(ratingRepository, never()).delete(any(Rating.class));
    }

    @Test
    void missingRatingCannotBeUpdatedOrDeleted() {
        assertThatThrownBy(() -> service.update(99L, 1L, new UpdateRatingRequest(3, null)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> service.delete(99L, 1L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(ratingRepository, never()).save(any());
        verify(ratingRepository, never()).delete(any(Rating.class));
    }

    private Rating existingRating() {
        return new Rating(owner, 550L, 5, "원래 후기");
    }
}
