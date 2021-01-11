#' Rotate entire pdf document
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to rotate the entire PDF document
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @param page_rotation An integer value from the vector c(0, 90, 180, 270).
#' Each option sets the page orientation as follows:
#' north: 0, east: 90, south: 180, west: 270. Note that the orientation cannot be
#' cummulatively changed (eg. 90 (east) will always turn the page so the beginning
#' of the page is on the right side)
#' @inheritParams input_filepath
#' @inheritParams output_filepath
#' @inheritParams overwrite
#' @return this function returns a PDF document with the rotated pages
#' @author Priyanga Dilini Talagala
#' @examples
#' \dontrun{
#' # This command prompts the user to select the file interactively.
#' # Rotate the entire PDF document to 90 degrees clockwise
#' rotate_pdf(page_rotation = 90)
#' }
#'
#' \dontrun{
#' if (requireNamespace("lattice", quietly = TRUE)) {
#' dir <- tempdir()
#' for(i in 1:3) {
#' pdf(file.path(dir, paste("plot", i, ".pdf", sep = "")))
#' print(lattice::xyplot(iris[,1] ~ iris[,i], data = iris))
#' dev.off()
#' }
#' output_file <- file.path(dir, paste('Full_pdf.pdf',  sep = ""))
#' staple_pdf(input_directory = dir, output_file)
#' input_path <- file.path(dir, paste("Full_pdf.pdf",  sep = ""))
#' output_path <-  file.path(dir, paste("rotated_pdf.pdf",  sep = ""))
#' rotate_pdf( page_rotation = 90,  input_path, output_path)
#' }
#' }
#' @export
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
rotate_pdf <- function(page_rotation = c(0, 90, 180, 270),  input_filepath = NULL, output_filepath = NULL, overwrite = TRUE) {

  page_rotation <- match.arg(as.character(page_rotation),c(0, 90, 180, 270))

  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }


  if(is.null(output_filepath)){
    #Choose output file interactively
    output_filepath <-  tcltk::tclvalue(tcltk::tkgetSaveFile(filetypes = '{Pdf {.pdf}}'))
  }

  input_filepath <- normalizePath(input_filepath, mustWork = TRUE)
  output_filepath <- normalizePath(output_filepath, mustWork = FALSE)

  rotation <- c("1-endnorth", "1-endeast", "1-endsouth", "1-endwest" )[match(page_rotation,c(0,90,180,270))]

  # Construct a system command to pdftk
  system_command <- paste(pdftk_cmd(),
                          shQuote(input_filepath),
                          "cat",
                          rotation,
                          "output",
                          "{shQuote(output_filepath)}",
                          sep = " ")
  # Invoke the command
  fileIO(input_filepath = input_filepath,
         output_filepath = output_filepath,
         overwrite = overwrite,
         system_command = system_command)

}
