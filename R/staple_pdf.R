#' Merge multiple PDF files into one
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to merge the PDF files.
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @param input_filepaths the path of the input PDF files.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#' @param output_directory the name of the output directory
#' @param output_filename the name of the output file.
#' @return this function returns a combined PDF document
#' @examples
#' \dontrun{
#' staple_pdf()
#' }
#' @export
#' @import utils
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
staple_pdf <- function(input_filepaths = NULL, output_directory = "output", output_filename = "Full_pdf") {

  if(is.null(input_filepaths)){
    #Choose a folder interactively
    path<- utils::choose.dir(default = "", caption = "Select folder")
    pwd <- getwd()
    setwd(path)
    # list all the pdf files in the selected folder
    input_filepaths <- (Sys.glob("*.pdf"))
  }

  #Create a folder to store output
  if(!dir.exists(output_directory)){
    dir.create(output_directory)
  }
  output_filepath<- file.path(output_directory, paste(output_filename,".pdf",  sep = ""))

  # Take the filepath arguments and format them for use in a system command
  quoted_names <- paste0('"', input_filepaths, '"')
  file_list <- paste(quoted_names, collapse = " ")
  output_filepath <- paste0('"', output_filepath, '"')

  # Construct a system command to pdftk
  system_command <- paste("pdftk",
                          file_list,
                          "cat",
                          "output",
                          output_filepath,
                          sep = " ")
  # Invoke the command
  system(command = system_command)
  setwd(pwd)
}
