import { Link, useNavigate, useSearch } from "@tanstack/react-router";
import type { FormEvent } from "react";
import { useQuery } from "@tanstack/react-query";
import { Footer } from "../../components/layout/footer";
import { BookmarkButton } from "../../components/movies/bookmark-button";
import { searchMovies } from "../../api/movies/search-movies";
import { getTmdbPosterUrl } from "../../utils/movies/tmdb-image";

export function SearchPage() {
  const { query = "" } = useSearch({ from: "/search" });
  const navigate = useNavigate({ from: "/search" });
  const normalizedQuery = query?.trim() ?? "";
  const searchQuery = useQuery({
    queryKey: ["movies", "search", { query: normalizedQuery }],
    queryFn: () => searchMovies({ query: normalizedQuery }),
    enabled: normalizedQuery.length > 0,
  });
  const searchResults = searchQuery.data?.results ?? [];

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const formData = new FormData(event.currentTarget);
    const nextQuery = String(formData.get("query") ?? "").trim();

    navigate({
      search: nextQuery ? { query: nextQuery } : {},
    });
  }

  if (normalizedQuery) {
    return (
      <>
        <main className="mx-auto min-h-[910px] w-full max-w-[1440px] bg-[#f6f7f9] px-5 pt-6 text-left text-[#17191e] md:px-[72px]">
          <div className="mx-auto w-full max-w-[1296px]">
            <h1 className="m-0 text-[28px] leading-[38px] font-bold tracking-[-0.56px] md:text-[32px] md:leading-[44px]">
              영화 검색
            </h1>

            <form
              className="mt-5 flex h-14 w-full items-center gap-3 border-y border-[#e3e6eb] bg-white px-4"
              onSubmit={handleSubmit}
            >
              <img
                className="h-6 w-6 shrink-0"
                src="/movie-icons/search.svg"
                alt=""
              />
              <input
                className="h-6 min-w-0 flex-1 appearance-none border-0 bg-transparent p-0 text-sm leading-5 text-[#17191e] outline-none placeholder:text-[#969da8]"
                key={query ?? ""}
                name="query"
                type="search"
                aria-label="영화 검색어"
                placeholder="예: 스파이더맨"
                defaultValue={query ?? ""}
              />
              <Link
                className="flex h-6 w-6 shrink-0 items-center justify-center rounded focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#3159c9]"
                to="/search"
                aria-label="검색어 지우기"
              >
                <img className="h-4 w-4" src="/movie-icons/close.svg" alt="" />
              </Link>
              <button
                className="h-[42px] shrink-0 cursor-pointer whitespace-nowrap rounded-lg border border-[#17191e] bg-[#17191e] px-4 text-center text-sm leading-[17px] font-extrabold text-white transition-colors hover:bg-[#2d3038] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#3182f6]"
                type="submit"
              >
                다시 검색
              </button>
            </form>

            <section className="mt-4" aria-live="polite">
              <div className="flex items-center justify-between gap-4">
                <h2 className="m-0 text-base leading-6 font-bold text-[#17191e] md:text-lg">
                  ‘{query?.trim()}’ 검색 결과
                </h2>
                <p className="m-0 text-xs leading-5 text-[#969da8]">
                  검색 {searchResults.length}편 · 1페이지
                </p>
              </div>

              {searchQuery.isLoading ? (
                <p className="my-20 text-center text-[#606774]" role="status">
                  검색 결과를 불러오는 중이에요.
                </p>
              ) : searchQuery.isError ? (
                <p className="my-20 text-center text-[#606774]" role="alert">{searchQuery.error.message}</p>
              ) : searchQuery.isSuccess && searchResults.length === 0 ? (
                <p className="my-20 text-center text-[#606774]">
                  검색 결과가 없어요.
                </p>
              ) : (
                <ul className="mt-4 grid list-none grid-cols-1 gap-x-8 p-0 md:grid-cols-2">
                  {searchResults.map((movie) => {
                    const posterUrl = getTmdbPosterUrl(movie.poster_path);
                    return (
                    <li
                      className="relative grid min-h-[218px] grid-cols-[104px_minmax(0,1fr)] gap-4 border-b border-[#e3e6eb] py-4 sm:grid-cols-[126px_minmax(0,1fr)] sm:gap-5"
                      key={movie.id}
                    >
                      <Link
                        className="block self-start"
                        to="/movies/$movieId"
                        params={{ movieId: String(movie.id) }}
                      >
                        {posterUrl ? (
                          <img
                            className="block h-[156px] w-[104px] rounded-lg object-cover sm:h-[190px] sm:w-[126px]"
                            src={posterUrl}
                            alt={`${movie.title} 포스터`}
                          />
                        ) : (
                          <div className="flex h-[156px] w-[104px] items-center justify-center rounded-lg bg-gray-200 text-sm text-gray-600 sm:h-[190px] sm:w-[126px]">
                            이미지 없음
                          </div>
                        )}
                      </Link>

                      <div className="min-w-0 pt-1">
                        <Link
                          className="block truncate text-base leading-6 font-bold text-[#17191e] no-underline hover:underline focus-visible:underline md:text-lg"
                          to="/movies/$movieId"
                          params={{ movieId: String(movie.id) }}
                        >
                          {movie.title}
                        </Link>
                        <p className="mt-1 mb-0 truncate text-xs leading-5 text-[#969da8]">
                          {movie.original_title} · {movie.release_date.replaceAll("-", ".")}
                        </p>
                        <p className="mt-3 mb-0 line-clamp-2 text-sm leading-6 text-[#606774]">
                          {movie.overview}
                        </p>
                        <Link
                          className="mt-4 inline-flex text-sm leading-5 font-semibold text-[#3159c9] no-underline hover:underline focus-visible:underline"
                          to="/movies/$movieId"
                          params={{ movieId: String(movie.id) }}
                        >
                          상세 보기 →
                        </Link>
                      </div>

                      <BookmarkButton movieId={movie.id} />
                    </li>
                    );
                  })}
                </ul>
              )}
            </section>
          </div>
        </main>
        <Footer />
      </>
    );
  }

  return (
    <main className="mx-auto min-h-[967px] w-full max-w-[1440px] bg-[#f6f7f9] px-5 pt-24 pb-12 text-left text-[#17191e] md:px-[72px] md:pt-[209px]">
      <div className="mx-auto flex w-full max-w-[790px] flex-col">
        <h1 className="m-0 text-center text-[34px] leading-[42px] font-bold tracking-[-1.4px] text-[#17191e] md:text-[46px] md:leading-[53px] md:tracking-[-1.84px]">
          어떤 영화를 찾고 있나요?
        </h1>

        <form
          className="mt-9 flex h-[74px] w-full items-center gap-3 self-center rounded-xl border-2 border-[#17191e] bg-white pr-[17px] pl-[21px] shadow-[0_12px_34px_rgba(17,19,24,0.08)]"
          onSubmit={handleSubmit}
        >
          <img className="h-6 w-6 shrink-0" src="/movie-icons/search.svg" alt="" />
          <input
            className="h-5 min-w-0 flex-1 appearance-none border-0 bg-transparent p-0 text-base leading-5 text-[#17191e] outline-none placeholder:text-[#969da8]"
            name="query"
            type="search"
            aria-label="영화 검색어"
            placeholder="예: 스파이더맨"
          />
          <button
            className="h-[42px] w-[59px] shrink-0 cursor-pointer whitespace-nowrap rounded-lg border border-[#17191e] bg-[#17191e] p-0 text-center text-sm leading-[17px] font-extrabold text-white transition-colors hover:bg-[#2d3038] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#3182f6]"
            type="submit"
          >
            검색
          </button>
        </form>
      </div>
    </main>
  );
}
