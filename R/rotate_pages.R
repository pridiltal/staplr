#' Rotate selected pages of a pdf file
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to rotate the given pages of
#' the seleted PDF files
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @param rotatepages a vector of page numbers to be rotated
#' @param page_rotation An integer value from the vector c(0, 90, 180, 270).
#' Each option sets the page rotation as follows (in degrees):
#' north: 0, east: 90, south: 180, west: 270
#' @param input_filepath the path of the input PDF file.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#' @param output_directory the path of the output directory
#' @param output_filename the name of the output file.
#' @return this function returns a PDF document with the
#' remaining pages
#' @author Priyanga Dilini Talagala
#' @examples
#' \dontrun{
#' # This command promts the user to select the file interactively.
#' # Rotate page 2 and 6 to 90 degrees clockwise
#' rotate_pages(rotatepages = c(3,6), page_rotation = 90)
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
#' staple_pdf(input_directory = dir, output_directory = dir, output_filename = "Full_pdf")
#' input_path <- file.path(dir, paste("Full_pdf.pdf",  sep = ""))
#' rotate_pages(rotatepages = c(2,3), page_rotation = 90,  input_path, output_directory = dir)
#' }
#' @export
#' @import utils
#' @importFrom  stringr str_extract
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
rotate_pages <- function(rotatepages, page_rotation, input_filepath = NULL, output_directory = NULL, output_filename = "rotated_pgs_pdf") {

  if(is.null(rotatepages) | !(page_rotation %in% c(0,90,180,270))){
    stop()
  }

  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }

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

  total <- 1:page_length

  keep <- total[-rotatepages]
  degree_0 <- split(keep, cumsum(seq_along(keep) %in% (which(diff(keep)>1)+1)))
  f<-function(x){paste(min(x),"-",max(x),sep = "")}
  degree_0 <-  as.vector(unlist(lapply(degree_0,f)))


  direction <- c("north", "east", "south", "west" )[match(page_rotation,c(0,90,180,270))]
  degree_x <- paste(rotatepages, direction, sep="")

  if(is.null(output_directory)){
    #Select a folder to store output
    output_directory<- tcltk::tk_choose.dir(caption = "Select directory to save output")
  }
  output_filepath<- file.path(output_directory, paste(output_filename,".pdf",  sep = ""))

  rotate <-vector(class(degree_0), length(c(degree_0,degree_x)))

  if(!(1 %in% rotatepages)) {
    rotate[c(TRUE, FALSE)] <-degree_0
    rotate[c( FALSE, TRUE)] <-degree_x
  } else {
    rotate[c(FALSE, TRUE)] <-degree_0
    rotate[c(TRUE, FALSE )] <-degree_x
  }

  # Construct a system command to pdftk
  system_command <- paste("pdftk",
                          shQuote(input_filepath),
                          "cat",
                          paste(rotate,collapse=" "),
                          "output",
                          shQuote(output_filepath),
                          sep = " ")
  # Invoke the command
  system(command = system_command)
}
