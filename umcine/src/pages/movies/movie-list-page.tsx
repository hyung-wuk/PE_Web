import { useEffect, useMemo, useState } from "react";
import { MovieGrid } from "../../components/movies/movie-grid";
import { Pagination } from "../../components/movies/pagination";
import { getMovies } from "../../api/movies/get-movies";
import type { TmdbMovieListItem } from "../../api/movies/models";

const MOVIES_PER_PAGE = 5;

export function MovieListPage() {
  const [movies, setMovies] = useState<TmdbMovieListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    let ignore = false;

    async function loadMovies() {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const response = await getMovies({ page: 1 });
        if (!ignore) setMovies(response.results);
      } catch {
        if (!ignore) setErrorMessage("영화 목록을 불러오지 못했어요.");
      } finally {
        if (!ignore) setIsLoading(false);
      }
    }

    loadMovies();
    return () => {
      ignore = true;
    };
  }, []);

  const totalPages = Math.ceil(movies.length / MOVIES_PER_PAGE);
  const visibleMovies = useMemo(() => {
    const startIndex = (currentPage - 1) * MOVIES_PER_PAGE;
    return movies.slice(startIndex, startIndex + MOVIES_PER_PAGE);
  }, [movies, currentPage]);

  return (
    <main className="mx-auto flex min-h-[1185px] w-full max-w-[1440px] flex-col gap-5 bg-[#f6f7f9] px-20 py-6 max-[1200px]:px-10 max-[768px]:min-h-0 max-[768px]:p-5">
      <h1 className="m-0 text-left text-[38px] leading-[44px] font-bold tracking-[-1.71px] text-[#17191e] max-[768px]:text-[30px] max-[768px]:leading-[38px] max-[768px]:tracking-[-1px]">
        영화 목록
      </h1>

      {isLoading ? (
        <p className="my-20 text-center text-[#606774]" role="status">영화 목록을 불러오는 중이에요.</p>
      ) : errorMessage ? (
        <p className="my-20 text-center text-[#606774]" role="alert">{errorMessage}</p>
      ) : movies.length === 0 ? (
        <p className="my-20 text-center text-[#606774]">조건에 맞는 영화가 없어요.</p>
      ) : (
        <>
          <MovieGrid movies={visibleMovies} />
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
          />
        </>
      )}
    </main>
  );
}
