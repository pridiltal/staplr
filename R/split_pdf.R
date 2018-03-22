#' Splits single input PDF document into individual pages.
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to Split a single input PDF document
#' into individual pages.
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @param input_filepath the path of the input PDF file.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#' @param output_directory the path of the output directory
#' @return this function splits a single input PDF document into
#' individual pages
#' @author Priyanga Dilini Talagala
#' @examples
#' \dontrun{
#' split_pdf()
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
#' staple_pdf(input_directory = dir, output_directory = dir)
#' split_pdf(input_filepath = file.path(dir, paste("Full_pdf.pdf",  sep = "")),output_directory = dir )
#' }
#' @export
#' @import utils
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
split_pdf <- function(input_filepath = NULL, output_directory = NULL) {

  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }

  if(is.null(output_directory)){
    #Select a folder to store output
    output_directory<- tcltk::tk_choose.dir(caption = "Select directory to save output")
  }

  # Take the filepath arguments and format them for use in a system command
  output_filepath <- shQuote(paste0(output_directory,"/page_%04d.pdf"))
  quoted_names <- shQuote( input_filepath)
  input_filepath <- paste(quoted_names, collapse = " ")

  # Construct a system command to pdftk
  system_command <- paste("pdftk",
                          input_filepath,
                          "burst",
                          "output",
                          output_filepath,
                          sep = " ")
  # Invoke the command
  system(command = system_command)

}



