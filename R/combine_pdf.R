#' Combine multiple PDF files
#' @description Combine multiple PDF files by delimiting the sequences of pages in each file.
#' @importFrom glue glue
#' @importFrom pdftools pdf_subset pdf_combine pdf_length
#' @importFrom purrr map2_dbl walk2 flatten_dbl map_if
#' @importFrom fs file_temp file_temp_push path_ext_remove path_file
#' @param vec_input Vector with paths of PDF files to be combined.
#' @param output PDF file path result of the combination.
#' @param start_pages Vector with the initial pages of each file. If \code{NA},
#' the default, will be considered the first page.
#' @param end_pages Vector with the final pages of each file. If \code{NA}, the
#' default, will be considered the last page.
#' @return
#' In the path informed in \code{output}, the PDF file resulting from the combination
#' of multiple files passed to \code{vec_output} will be saved.
#' @export
#' @examples
#'
#' \dontrun{
#' combine_pdf(
#'    vec_input =
#'       c(
#'         "file_1.pdf",
#'         "file_2.pdf",
#'       ),
#'    output = "output.pdf",
#'    start_pages = c(NA, NA),
#'    end_pages = c(NA, NA)
#'  )
#' }

combine_pdf <- function(vec_input, output = "output.pdf", start_pages = NA, end_pages = NA) {

  if(length(start_pages) != length(vec_input) || length(end_pages) != length(vec_input))
    stop("Start_pages and end_pages must be a vector of the same length as vec_input!")

  start_pages <-
    flatten_dbl(
      map_if(
        .x = start_pages,
        .f = function(x) 1,
        .p = is.na
      )
    )

  f <- function(x, y)
    ifelse(
      is.na(x),
      pdf_length(vec_input[y]),
      x
    )

  end_pages <-
    purrr::map2_dbl(
      .x = end_pages,
      .y = seq_along(vec_input),
      .f = f
    )

  files <-
    file_temp_push(
      glue(
        "{tempdir()}/{path_ext_remove(path_file(output))}_{1L:length(vec_input)}.pdf"
      )
    )

  one_step <- function(x, y) {
    pdf_subset(
        input = x,
        output = file_temp(),
        pages = start_pages[y]:end_pages[y]
      )
  }

  walk2(
    .x = vec_input,
    .y = 1L:length(vec_input),
    .f = ~ one_step(.x, .y)
  )

  pdf_combine(input = files, output = output)
}
