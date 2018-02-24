#' Splits a single input PDF document into individual pages.
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to Split a single input PDF document
#' into individual pages.
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @param input_filepath the path of the input PDF file.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#' @return this function splits a single input PDF document into
#' individual pages
#' @examples
#' \dontrun{
#' split_pdf()
#' }
#' @export
#' @import utils
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
split_pdf <- function(input_filepath = NULL) {

  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    file <- file.choose(new = FALSE)
    path <- dirname(file)
    file_name <- basename(file)
    pwd <- getwd()
    setwd(path)
  }

  #Select a folder to store output
  path_out<- tcltk::tk_choose.dir(caption = "Select directory to save output")

  # Take the filepath arguments and format them for use in a system command
  output_filepath <- paste0('"', basename(path_out),"/page_%04d.pdf", '"')

  # Construct a system command to pdftk
  system_command <- paste("pdftk",
                          file_name,
                          "burst",
                          "output",
                          output_filepath,
                          sep = " ")
  # Invoke the command
  system(command = system_command)
  setwd(pwd)
}



