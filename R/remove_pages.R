#' Remove selected pages from a file
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to remove the given pages from
#' the seleted PDF files.
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @param rmpages a vector of page numbers to be removed
#' @param input_filepaths the path of the input PDF files.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#' @param output_directory the name of the output directory
#' @param output_filename the name of the output file.
#' @return this functin returns a PDF document with the
#' remaining pages
#' @examples
#' \dontrun{
#' # This command promts the user to select the file interactively.
#' # Remove page 2 and 3 from the selected file.
#' remove_pages(rmpages = c(3,6))
#' }
#' @export
#' @import utils
#' @importFrom pdftools pdf_info
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
remove_pages <- function(rmpages, input_filepaths = NULL, output_directory = "trim", output_filename = "trimmed_pdf") {

  if(is.null(rmpages)){
    stop()
  }

  if(is.null(input_filepaths)){
    #Choose the pdf file interactively
    file <- file.choose(new = FALSE)
    path <- dirname(file)
    file_name <- basename(file)
    setwd(path)
  }

  pdfInfo <- pdftools::pdf_info(file)
  total <- 1:pdfInfo$pages
  keep <- total[-rmpages]
  selected_pages <- split(keep, cumsum(seq_along(keep) %in%
                                      (which(diff(keep)>1)+1)))
  f<-function(x){paste(min(x),"-",max(x),sep = "")}
  selected_pages <- lapply(selected_pages,f)

  #Create a folder to store output
  if(!dir.exists(output_directory)){
    dir.create(output_directory)
  }
  output_filepath<- file.path(output_directory, paste(output_filename,".pdf",  sep = ""))

  # Take the filepath arguments and format them for use in a system command
  selected_pages <- (unlist(selected_pages))
  selected_pages <- paste(selected_pages,collapse=" ")
  output_filepath <- paste0('"', output_filepath, '"')

  # Construct a system command to pdftk
  system_command <- paste("pdftk",
                          file_name,
                          "cat",
                          selected_pages,
                          "output",
                          output_filepath,
                          sep = " ")
  # Invoke the command
  system(command = system_command)

}


