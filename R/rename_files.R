#' Rename multiple files
#'
#' @description Rename multiple files in a directory and
#' write renamed files to a new directory
#'
#' @param input_filepaths the path of the input PDF files.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#' @param new_names a vector of names for the output files.
#' @param output_directory the name of the output directory
#' @return this function writes renamed files to a new directory
#' @examples
#' \dontrun{
#' #if the directory contains 3 PDF files
#' rename_files(new_names = c("file 1", "file 2", "file 3"))
#' }
#' @export
#' @import utils
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
rename_files <- function(input_filepaths = NULL, new_names,
                         output_directory = "rename") {
  if(is.null(new_names)){
    stop()
  }
  if(is.null(input_filepaths)){
    #Choose a folder interactively
    path<- utils::choose.dir(default = "", caption = "Select folder")
    setwd(path)
    # list all the pdf files in the selected folder
    input_filepaths <- (Sys.glob("*.pdf"))
  }

  #Create a folder to store output
  if(!dir.exists(output_directory)){
    dir.create(output_directory)
  }

  # Take the filepath arguments and format them for use in a system command
  output_filepath <-  file.path(output_directory, paste(new_names,".pdf",  sep = ""))

  file.rename(input_filepaths, output_filepath)

}
