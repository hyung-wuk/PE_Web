import { cn } from "../../utils/cn";

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function Pagination({
  currentPage,
  totalPages,
  onPageChange,
}: PaginationProps) {
  const pageCount = Math.min(5, totalPages);
  const startPage = Math.max(1, Math.min(currentPage - 2, totalPages - pageCount + 1));
  const pages = Array.from({ length: pageCount }, (_, index) => startPage + index);

  const handlePrevious = () => {
    if (currentPage > 1) {
      onPageChange(currentPage - 1);
    }
  };

  const handleNext = () => {
    if (currentPage < totalPages) {
      onPageChange(currentPage + 1);
    }
  };

  return (
    <nav
      className="flex h-9 w-full items-center justify-center gap-3"
      aria-label="영화 목록 페이지"
    >
      <button
        type="button"
        className="flex h-6 w-6 cursor-pointer items-center justify-center border-0 bg-transparent p-0 disabled:cursor-default disabled:opacity-30"
        onClick={handlePrevious}
        disabled={currentPage === 1}
        aria-label="이전 페이지"
      >
        <img
          className="block h-6 w-6"
          src="/movie-icons/chevron-left.svg"
          alt=""
        />
      </button>

      <div className="flex h-9 w-[196px] items-center justify-center gap-1">
        {pages.map((page) => (
          <button
            type="button"
            key={page}
            className={cn(
              "flex h-9 w-9 cursor-pointer items-center justify-center rounded-lg border-0 bg-transparent p-0 text-sm font-medium text-[#6b7280] hover:bg-[#e9ecef]",
              currentPage === page &&
                "bg-[#3182f6] text-white hover:bg-[#3182f6]",
            )}
            onClick={() => onPageChange(page)}
            aria-current={currentPage === page ? "page" : undefined}
          >
            {page}
          </button>
        ))}
      </div>

      <button
        type="button"
        className="flex h-6 w-6 cursor-pointer items-center justify-center border-0 bg-transparent p-0 disabled:cursor-default disabled:opacity-30"
        onClick={handleNext}
        disabled={currentPage === totalPages}
        aria-label="다음 페이지"
      >
        <img
          className="block h-6 w-6"
          src="/movie-icons/chevron-right.svg"
          alt=""
        />
      </button>
    </nav>
  );
}
