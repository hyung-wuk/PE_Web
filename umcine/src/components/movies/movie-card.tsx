import { Link } from "@tanstack/react-router";
import type { TmdbMovieListItem } from "../../api/movies/models";
import { BookmarkButton } from "./bookmark-button";
import { getTmdbPosterUrl } from "../../utils/movies/tmdb-image";

interface MovieCardProps {
  movie: TmdbMovieListItem;
}

export function MovieCard({ movie }: MovieCardProps) {
  const posterUrl = getTmdbPosterUrl(movie.poster_path);
  return (
    <article className="flex w-full min-w-0 flex-col gap-1 overflow-visible">
      <div className="relative h-[274px] w-full shrink-0 overflow-hidden rounded-[10px] bg-[#f6f7f9] max-[900px]:aspect-[241.6/274] max-[900px]:h-auto max-[600px]:rounded-lg">
        <Link
          className="block h-full w-full"
          to="/movies/$movieId"
          params={{ movieId: String(movie.id) }}
        >
          {posterUrl ? (
            <img
              className="block h-full w-full rounded-[inherit] object-cover object-center"
              src={posterUrl}
              alt={`${movie.title} 포스터`}
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center bg-gray-200 text-sm text-gray-600">
              이미지 없음
            </div>
          )}
        </Link>

        <BookmarkButton movieId={movie.id} />
      </div>

      <div className="flex w-full min-w-0 flex-col">
        <Link
          className="group text-inherit no-underline"
          to="/movies/$movieId"
          params={{ movieId: String(movie.id) }}
        >
          <h2 className="m-0 w-full overflow-hidden text-ellipsis whitespace-nowrap text-sm leading-5 font-semibold text-[#17191e] group-hover:underline group-focus-visible:underline max-[600px]:text-[13px] max-[600px]:leading-[18px]">
            {movie.title}
          </h2>
        </Link>
        <p className="m-0 w-full overflow-hidden text-ellipsis whitespace-nowrap text-xs leading-4 font-normal text-[#9ca3af] max-[600px]:text-[11px] max-[600px]:leading-[15px]">
          {movie.release_date.replaceAll("-", ".")}
        </p>
      </div>
    </article>
  );
}
