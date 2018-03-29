#' Merge multiple PDF files into one
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to merge the PDF files.
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @param input_directory the path of the input PDF files.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#' @param output_filepath the path of the output output PDF file.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactivelye.
#' @return this function returns a combined PDF document
#' @author Priyanga Dilini Talagala
#' @examples
#' \dontrun{
#' staple_pdf()
#' }
#'
#' \dontrun{
#' dir <- tempdir()
#' require(lattice)
#' for(i in 1:3) {
#' pdf(file.path(dir, paste("plot", i, ".pdf", sep = "")))
#' print(xyplot(iris[,1] ~ iris[,i], data = iris))
#' dev.off()
#' }
#' output_file <- file.path(dir, paste('Full_pdf.pdf',  sep = ""))
#' staple_pdf(input_directory = dir, output_file)
#' }
#' @export
#' @importFrom tcltk tk_choose.dir
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
staple_pdf <- function(input_directory = NULL, output_filepath = NULL) {

  if(is.null(input_directory)){
    #Choose a folder interactively
    input_directory<- tcltk::tk_choose.dir(caption = "Select directory which contains PDF fies")
  }

  # list all the pdf files in the selected folder
  input_filepaths <- (Sys.glob(file.path(input_directory,"*.pdf")))

  if(is.null(output_filepath)){
    #Choose output file interactively
    output_filepath <-  tcltk::tclvalue(tcltk::tkgetSaveFile(filetypes = '{Pdf {.pdf}}'))
  }

  # Construct a system command to pdftk
  system_command <- paste("pdftk",
                          paste(shQuote(input_filepaths), collapse = " "),
                          "cat",
                          "output",
                          shQuote(output_filepath),
                          sep = " ")
  # Invoke the command
  system(command = system_command)
}
