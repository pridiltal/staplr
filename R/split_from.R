#' Splits single input PDF document into two parts from a given point
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to Split a single input PDF document
#' into two parts from a given point
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @param pg_num a nonnegative integer. Split the pdf document into two from this page number.
#' @param input_filepath the path of the input PDF file.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#' @param output_directory the path of the output directory
#' @return this function splits a single input PDF document into
#' individual pages
#' @author Priyanga Dilini Talagala
#' @examples
#' \dontrun{
#' # Split the pdf from page 10
#' split_from(pg_num=10)
#' }
#'
#' \dontrun{
#' dir <- tempdir()
#' require(lattice)
#' for(i in 1:4) {
#' pdf(file.path(dir, paste("plot", i, ".pdf", sep = "")))
#' print(xyplot(iris[,1] ~ iris[,i], data = iris))
#' dev.off()
#' }
#' staple_pdf(input_directory = dir, output_filepath = file.path(dir, 'Full_pdf.pdf'))
#' input_path <- file.path(dir, "Full_pdf.pdf")
#' split_from(pg_num=2, input_filepath = input_path ,output_directory = dir )
#' }
#' @export
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
split_from <- function(pg_num, input_filepath = NULL, output_directory = NULL, prefix = 'part') {

  assertthat::assert_that(assertthat::is.number(pg_num))

  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }

  if(is.null(output_directory)){
    #Select a folder to store output
    output_directory<- tcltk::tk_choose.dir(caption = "Select directory to save output")
  }

  # Take the filepath arguments and format them for use in a system command
  output_filepath_1 <- file.path(output_directory, paste(prefix,"1.pdf",  sep = ""))
  output_filepath_2 <- file.path(output_directory, paste(prefix,"2.pdf",  sep = ""))

  # Construct a system command to pdftk to save the first part
  system_command <- paste("pdftk",
                          shQuote(input_filepath),
                          "cat",
                          paste("1-",pg_num, sep = ""),
                          "output",
                          shQuote(output_filepath_1),
                          sep = " ")
  # Invoke the command
  system(command = system_command)

  # Construct a system command to pdftk to save the second part
  system_command <- paste("pdftk",
                          shQuote(input_filepath),
                          "cat",
                          paste(pg_num+1, "-end",sep = ""),
                          "output",
                          shQuote(output_filepath_2),
                          sep = " ")
  # Invoke the command
  system(command = system_command)

}



