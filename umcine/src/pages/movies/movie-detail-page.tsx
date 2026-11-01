import { Link, useParams } from "@tanstack/react-router";
import { useEffect, useState, type FormEvent } from "react";
import { BookmarkButton } from "../../components/movies/bookmark-button";
import { getMovieDetail } from "../../api/movies/get-movie-detail";
import type { TmdbMovieDetail } from "../../api/movies/models";
import { getTmdbPosterUrl, getTmdbBackdropUrl } from "../../utils/movies/tmdb-image";
import { getMovieDetailErrorMessage } from "../../utils/movies/get-movie-detail-error-message";

export function MovieDetailPage() {
  const { movieId } = useParams({ from: "/movies/$movieId" });
  const parsedMovieId = Number(movieId);
  const isValidMovieId = Number.isInteger(parsedMovieId) && parsedMovieId > 0;
  const [movie, setMovie] = useState<TmdbMovieDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [rating, setRating] = useState(0);
  const [savedRating, setSavedRating] = useState<number | null>(null);

  useEffect(() => {
    let ignore = false;

    async function loadMovie() {
      setMovie(null);
      setErrorMessage(null);
      setRating(0);
      setSavedRating(null);

      if (!Number.isInteger(parsedMovieId) || parsedMovieId <= 0) {
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      try {
        const response = await getMovieDetail(parsedMovieId);
        if (!ignore) setMovie(response);
      } catch (error) {
        if (!ignore) setErrorMessage(getMovieDetailErrorMessage(error));
      } finally {
        if (!ignore) setIsLoading(false);
      }
    }

    loadMovie();
    return () => {
      ignore = true;
    };
  }, [parsedMovieId]);

  if (isValidMovieId && isLoading) {
    return (
      <main className="mx-auto flex min-h-[910px] w-full max-w-[1440px] items-center justify-center bg-[#f6f7f9] px-5 text-[#606774]">
        <p role="status">영화 정보를 불러오는 중이에요.</p>
      </main>
    );
  }

  if (!isValidMovieId || errorMessage || !movie) {
    const message = !isValidMovieId
      ? "올바르지 않은 영화 번호예요."
      : errorMessage ?? "영화를 찾을 수 없어요.";
    return (
      <main className="mx-auto flex min-h-[910px] w-full max-w-[1440px] flex-col items-center justify-center bg-[#f6f7f9] px-5 text-center text-[#17191e]">
        <h1 className="m-0 text-[32px] leading-[44px] font-bold tracking-[-0.64px]" role="alert">
          {message}
        </h1>
        <p className="mt-3 mb-8 text-base leading-6 text-[#606774]">
          영화 번호와 연결 상태를 확인한 뒤 다시 시도해 주세요.
        </p>
        <Link
          className="rounded-lg bg-[#3182f6] px-5 py-3 text-sm font-semibold text-white no-underline"
          to="/"
        >
          영화 목록으로 돌아가기
        </Link>
      </main>
    );
  }

  const posterUrl = getTmdbPosterUrl(movie.poster_path);
  const backdropUrl = getTmdbBackdropUrl(movie.backdrop_path);
  const runtimeText = typeof movie.runtime === "number"
    ? `${Math.floor(movie.runtime / 60)}시간 ${movie.runtime % 60}분`
    : "상영 시간 정보가 없어요.";

  function handleRatingSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSavedRating(rating);
  }

  return (
    <main className="mx-auto min-h-[910px] w-full max-w-[1440px] bg-[#f6f7f9] text-[#17191e]">
      <section
        className="relative h-[360px] overflow-hidden bg-gray-200"
        aria-label={`${movie.title} 배경 이미지`}
      >
        {backdropUrl ? (
          <img className="absolute inset-0 h-full w-full object-cover" src={backdropUrl} alt="" aria-hidden="true" />
        ) : (
          <div className="absolute inset-0 flex items-center justify-center text-gray-600">이미지 없음</div>
        )}
        <div className="absolute inset-0 bg-[linear-gradient(90deg,rgba(17,19,24,0.78)_0%,rgba(17,19,24,0.42)_45%,rgba(17,19,24,0.06)_100%)]" />

        <div className="relative mx-auto flex h-full w-[calc(100%-40px)] max-w-[1280px] flex-col py-7 text-white">
          <Link
            className="inline-flex w-fit items-center gap-1 text-sm leading-5 font-semibold text-white no-underline hover:underline focus-visible:underline"
            to="/"
          >
            <img
              className="h-5 w-5 brightness-0 invert"
              src="/movie-icons/chevron-left.svg"
              alt=""
            />
            영화 목록
          </Link>

          <div className="mt-auto pb-4">
            <h1 className="m-0 text-[34px] leading-[44px] font-bold tracking-[-1.2px] md:text-[38px]">
              {movie.title}
            </h1>
            <p className="mt-2 mb-0 text-sm leading-5 font-medium text-white/80">
              {movie.original_title}
            </p>
            <p className="mt-1 mb-0 text-sm leading-5 text-white/80">
              {movie.release_date.replaceAll("-", ".")} · {movie.genres.map((genre) => genre.name).join(", ")} · {runtimeText}
            </p>
          </div>
        </div>
      </section>

      <section className="mx-auto grid min-h-[550px] w-[calc(100%-40px)] max-w-[1280px] grid-cols-1 gap-8 py-6 md:grid-cols-[200px_minmax(0,1fr)] lg:grid-cols-[200px_minmax(0,1fr)_320px] lg:gap-8">
        {posterUrl ? (
          <img
            className="block h-[288px] w-[200px] rounded-lg object-cover shadow-[0_12px_30px_rgba(17,19,24,0.14)]"
            src={posterUrl}
            alt={`${movie.title} 포스터`}
          />
        ) : (
          <div className="flex h-[288px] w-[200px] items-center justify-center rounded-lg bg-gray-200 text-sm text-gray-600">
            이미지 없음
          </div>
        )}

        <div className="min-w-0 pt-1">
          <h2 className="m-0 text-xl leading-7 font-bold tracking-[-0.3px]">
            {movie.tagline}
          </h2>
          <p className="mt-4 mb-0 max-w-[650px] text-sm leading-6 text-[#606774]">
            {movie.overview}
          </p>
          <p className="mt-3 mb-0 max-w-[650px] text-sm leading-6 text-[#606774]">
            {movie.title}에서 펼쳐지는 새로운 이야기와 인물들의 선택을 만나보세요.
          </p>

          <div className="relative mt-5 h-10 w-10">
            <BookmarkButton movieId={movie.id} />
          </div>
        </div>

        <aside className="border-t border-[#e3e6eb] pt-6 lg:border-t-0 lg:border-l lg:pt-1 lg:pl-8">
          <h2 className="m-0 text-xl leading-7 font-bold">내 평점</h2>
          <p className="mt-1 mb-0 text-xs leading-5 text-[#969da8]">
            별점을 선택하고 감상을 남겨보세요.
          </p>

          <form className="mt-4" onSubmit={handleRatingSubmit}>
            <div className="flex gap-2" role="radiogroup" aria-label="영화 평점">
              {[1, 2, 3, 4, 5].map((score) => (
                <button
                  className="flex h-9 w-9 items-center justify-center border-0 bg-transparent p-0 focus-visible:rounded focus-visible:outline-2 focus-visible:outline-[#3159c9]"
                  key={score}
                  type="button"
                  role="radio"
                  aria-label={`${score}점`}
                  aria-checked={rating === score}
                  onClick={() => setRating(score)}
                >
                  <img
                    className="h-6 w-6 opacity-55"
                    src={
                      score <= rating
                        ? "/movie-icons/star.svg"
                        : "/movie-icons/star-outline.svg"
                    }
                    alt=""
                  />
                </button>
              ))}
            </div>

            <textarea
              key={movie.id}
              className="mt-4 h-[100px] w-full resize-none rounded-lg border border-[#e3e6eb] bg-white p-3 text-sm leading-5 text-[#17191e] outline-none placeholder:text-[#969da8] focus:border-[#3159c9]"
              name="review"
              aria-label="한줄평"
              placeholder="영화를 보고 느낀 점을 남겨보세요."
            />

            <button
              className="mt-3 h-10 w-full rounded-lg border-0 bg-[#17191e] text-sm font-semibold text-white transition-colors hover:bg-[#2d3038] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#17191e]"
              type="submit"
            >
              평점 저장
            </button>

            <p className="mt-2 min-h-5 text-xs leading-5 text-[#3159c9]" aria-live="polite">
              {savedRating ? `${savedRating}점으로 저장했어요.` : ""}
            </p>
          </form>
        </aside>
      </section>
    </main>
  );
}
