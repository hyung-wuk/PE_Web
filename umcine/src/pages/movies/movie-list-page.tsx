import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { MovieGrid } from "../../components/movies/movie-grid";
import { Pagination } from "../../components/movies/pagination";
import { getMovies } from "../../api/movies/get-movies";

export function MovieListPage() {
  const [page, setPage] = useState(1);
  const moviesQuery = useQuery({
    queryKey: ["movies", "list", { page }],
    queryFn: () => getMovies({ page }),
  });

  const movies = moviesQuery.data?.results ?? [];
  const totalPages = Math.min(moviesQuery.data?.total_pages ?? 0, 500);

  const handlePageChange = (nextPage: number) => {
    if (Number.isInteger(nextPage) && nextPage >= 1 && nextPage <= totalPages) {
      setPage(nextPage);
    }
  };

  return (
    <main className="mx-auto flex min-h-[1185px] w-full max-w-[1440px] flex-col gap-5 bg-[#f6f7f9] px-20 py-6 max-[1200px]:px-10 max-[768px]:min-h-0 max-[768px]:p-5">
      <h1 className="m-0 text-left text-[38px] leading-[44px] font-bold tracking-[-1.71px] text-[#17191e] max-[768px]:text-[30px] max-[768px]:leading-[38px] max-[768px]:tracking-[-1px]">
        영화 목록
      </h1>

      {moviesQuery.isPending ? (
        <p className="my-20 text-center text-[#606774]" role="status">영화 목록을 불러오는 중이에요.</p>
      ) : moviesQuery.isError ? (
        <p className="my-20 text-center text-[#606774]" role="alert">{moviesQuery.error.message}</p>
      ) : movies.length === 0 ? (
        <p className="my-20 text-center text-[#606774]">조건에 맞는 영화가 없어요.</p>
      ) : (
        <>
          <MovieGrid movies={movies} />
          <Pagination
            currentPage={page}
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
        </>
      )}
    </main>
  );
}
