import type { TmdbMovieListItem } from "../../api/movies/models";
import { MovieCard } from "./movie-card";

interface MovieGridProps {
  movies: TmdbMovieListItem[];
}

export function MovieGrid({ movies }: MovieGridProps) {
  return (
    <div className="grid w-full grid-cols-5 items-start gap-x-[18px] gap-y-5 max-[1100px]:grid-cols-4 max-[850px]:grid-cols-3 max-[850px]:gap-x-4 max-[850px]:gap-y-[18px] max-[600px]:grid-cols-2 max-[600px]:gap-x-3 max-[600px]:gap-y-4 max-[380px]:grid-cols-1">
      {movies.map((movie) => (
        <MovieCard key={movie.id} movie={movie} />
      ))}
    </div>
  );
}
