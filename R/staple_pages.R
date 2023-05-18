#' Combine multiple PDF files
#' @description Combine multiple PDF files by delimiting the sequences of pages in each file.
#' @importFrom glue glue
#' @importFrom pdftools pdf_subset pdf_combine pdf_length
#' @importFrom purrr map2_dbl walk2 flatten_dbl map_if
#' @importFrom fs file_temp file_temp_push path_ext_remove path_file
#' @param input_files Vector with paths of PDF files to be combined.
#' @param output_filepath PDF file path result of the combination.
#' @param start_pages Vector with the initial pages of each file. If \code{NA},
#' the default, will be considered the first page.
#' @param end_pages Vector with the final pages of each file. If \code{NA}, the
#' default, will be considered the last page.
#' @return
#' In the path informed in \code{output_filepath}, the PDF file resulting from the combination
#' of multiple files passed to \code{input_files} will be saved.
#' @author Pedro Rafael D. Marinho
#' @export
#' @examples
#'
#' \dontrun{
#' staple_pages(
#'    input_files =
#'       c(
#'         "file_1.pdf",
#'         "file_2.pdf",
#'       ),
#'    output_filepath = "output.pdf",
#'    start_pages = c(NA, NA),
#'    end_pages = c(NA, NA)
#'  )
#' }

staple_pages <- function(input_files, output_filepath = "output.pdf", start_pages = NA, end_pages = NA) {

  if(length(start_pages) != length(input_files) || length(end_pages) != length(input_files))
    stop("Start_pages and end_pages must be a vector of the same length as input_files!")

  start_pages <-
    purrr::flatten_dbl(
      purrr::map_if(
        .x = start_pages,
        .f = function(x) 1,
        .p = is.na
      )
    )

  f <- function(x, y)
    ifelse(
      is.na(x),
      pdftools::pdf_length(input_files[y]),
      x
    )

  end_pages <-
    purrr::map2_dbl(
      .x = end_pages,
      .y = seq_along(input_files),
      .f = f
    )

  files <-
    fs::file_temp_push(
      glue(
        "{tempdir()}/{fs::path_ext_remove(fs::path_file(output_filepath))}_{1L:length(input_files)}.pdf"
      )
    )

  one_step <- function(x, y) {
    pdftools::pdf_subset(
        input = x,
        output = fs::file_temp(),
        pages = start_pages[y]:end_pages[y]
      )
  }

  purrr::walk2(
    .x = input_files,
    .y = 1L:length(input_files),
    .f = ~ one_step(.x, .y)
  )

  pdftools::pdf_combine(input = files, output = output_filepath)
}
