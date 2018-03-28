#' Remove selected pages from a file
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to remove the given pages from
#' the seleted PDF files.
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @param rmpages a vector of page numbers to be removed
#' @param input_filepath the path of the input PDF file.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#' @param output_directory the path of the output directory
#' @param output_filename the name of the output file.
#' @param page_length page count of the original file
#' @return this function returns a PDF document with the
#' remaining pages
#' @author Priyanga Dilini Talagala
#' @examples
#' \dontrun{
#' # This command promts the user to select the file interactively.
#' # Remove page 2 and 3 from the selected file.
#' remove_pages(rmpages = c(3,6))
#' }
#'
#' \dontshow{
#' dir <- tempdir()
#' require(lattice)
#' for(i in 1:3) {
#' pdf(file.path(dir, paste("plot", i, ".pdf", sep = "")))
#' print(xyplot(iris[,1] ~ iris[,i], data = iris))
#' dev.off()
#' }
#' staple_pdf(input_directory = dir, output_directory = dir, output_filename = "Full_pdf")
#' remove_pages(rmpages = c(1), input_filepath = file.path(dir, paste("Full_pdf.pdf",  sep = "")),
#'  output_directory = dir, page_length = 3)
#' }
#' @export
#' @import utils
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
remove_pages <- function(rmpages, input_filepath = NULL, output_directory = NULL, output_filename = "trimmed_pdf", page_length = NULL) {

  if(is.null(rmpages)){
    stop()
  }

  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }

  readinteger <- function()
  {
    n <- readline(prompt="Enter page count: ")
    return(as.integer(n))
  }

  if(is.null(page_length)){
    total <- 1:readinteger()
  } else {
    total <- 1:page_length
  }
  keep <- total[-rmpages]
  selected_pages <- split(keep, cumsum(seq_along(keep) %in%
                                      (which(diff(keep)>1)+1)))
  f<-function(x){paste(min(x),"-",max(x),sep = "")}
  selected_pages <- lapply(selected_pages,f)

  if(is.null(output_directory)){
    #Select a folder to store output
    output_directory<- tcltk::tk_choose.dir(caption = "Select directory to save output")
  }
  output_filepath<- file.path(output_directory, paste(output_filename,".pdf",  sep = ""))

  # Construct a system command to pdftk
  system_command <- paste("pdftk",
                          shQuote(input_filepath),
                          "cat",
                          paste(unlist(selected_pages),collapse=" "),
                          "output",
                          shQuote(output_filepath),
                          sep = " ")
  # Invoke the command
  system(command = system_command)
}
