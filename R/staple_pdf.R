#' Merge multiple PDF files into one
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to merge the PDF files.
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @param input_directory the path of the input PDF files.
#' The default is set to NULL. If NULL, it  prompt the user to
#' select the folder interactively.
#' @param input_files a vector of input PDF files. The default is set to NULL. If NULL and \code{input_directory} is also NULL, the user is propted to select a folder interactively.
#' @param output_filepath the path of the output output PDF file.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#' @return this function returns a combined PDF document
#' @author Priyanga Dilini Talagala and Daniel Padfield
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
staple_pdf <- function(input_directory = NULL, input_files = NULL,
                       output_filepath = NULL)
{
  # set error if neither input_directory of input_files are null
  if(!is.null(input_directory) & !is.null(input_files)){
    stop("One of input_directory and input_files has to be NULL.")
  }

  if(is.null(input_directory) & is.null(input_files)) {
    input_directory <- tcltk::tk_choose.dir(caption = "Select directory which contains PDF fies")
  }
  if(!is.null(input_directory)){input_filepaths <- (Sys.glob(file.path(input_directory, "*.pdf")))}
  if(!is.null(input_files)){input_filepaths <- input_files}

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

  system(command = system_command)
}
