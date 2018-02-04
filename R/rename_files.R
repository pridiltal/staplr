#' Rename multiple files
#'
#' @description Rename multiple files in a directory and
#' write renamed files back to directory
#'
#' @param input_filepaths the path of the input PDF files.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#' @param new_names a vector of names for the output files.
#' @return this function writes renamed files to a new directory
#' @examples
#' \dontrun{
#' #if the directory contains 3 PDF files
#' rename_files(new_names = c("file 1", "file 2", "file 3"))
#' }
#' @export
#' @import utils
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
rename_files <- function(input_filepaths = NULL, new_names) {
  if(is.null(new_names)){
    stop()
  }
  if(is.null(input_filepaths)){
    #Choose a folder interactively
    path<- utils::choose.dir(default = "", caption = "Select folder")
    pwd <- getwd()
    setwd(path)
    # list all the pdf files in the selected folder
    input_filepaths <- (Sys.glob("*.pdf"))
  }

  # Take the filepath arguments and format them for use in a system command
  output_filepath <-  paste(new_names,".pdf",  sep = "")

  file.rename(input_filepaths, output_filepath)
  setwd(pwd)
}
