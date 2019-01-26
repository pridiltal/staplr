#' Splits single input PDF document into individual pages.
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to Split a single input PDF document
#' into individual pages.
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @inheritParams input_filepath
#' @param output_directory the path of the output directory
#' @param prefix A string for output filename prefix
#' @return this function splits a single input PDF document into
#' individual pages
#' @author Priyanga Dilini Talagala and Ogan Mancarci
#' @examples
#' \dontrun{
#' split_pdf()
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
#' staple_pdf(input_directory = dir, output_filepath = file.path(dir, 'Full_pdf.pdf'))
#' split_pdf(input_filepath = file.path(dir, paste("Full_pdf.pdf",  sep = "")),output_directory = dir )
#' }
#' @export
#' @import utils
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
split_pdf <- function(input_filepath = NULL, output_directory = NULL, prefix = 'page_') {

  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }

  if(is.null(output_directory)){
    #Select a folder to store output
    output_directory<- tcltk::tk_choose.dir(caption = "Select directory to save output")
  }

  input_filepath <- normalizePath(input_filepath, mustWork = TRUE)
  output_directory <- normalizePath(output_directory, mustWork =  TRUE)

  # Getting the page count to add the correct amout of zeroes to make it scalable
  metadataTemp <- tempfile()
  # Construct a system command to pdftk to get number of pages
  system_command <- paste("pdftk",
                          shQuote(input_filepath),
                          "dump_data",
                          "output",
                          shQuote(metadataTemp))
  system(command = system_command)
  page_length <- as.numeric(stringr::str_extract(grep( "NumberOfPages", paste0(readLines(metadataTemp)),
                                                       value = TRUE), "\\d+$"))
  digits <- max(floor(log(page_length)),4)


  # Take the filepath arguments and format them for use in a system command
  output_filepath <- shQuote(paste0(output_directory, "/", prefix, "%0",digits,"d.pdf"))
  # Construct a system command to pdftk
  system_command <- paste("pdftk",
                          shQuote(input_filepath),
                          "burst",
                          "output",
                          output_filepath,
                          sep = " ")
  # Invoke the command
  system(command = system_command)

}



