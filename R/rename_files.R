#' Rename multiple files
#'
#' @description Rename multiple files in a directory and
#' write renamed files back to directory
#'
#' @param input_directory the path of the input PDF files.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#' @param new_names a vector of names for the output files.
#' @return this function writes renamed files back to directory
#' @author Priyanga Dilini Talagala
#' @examples
#' \dontrun{
#' #if the directory contains 3 PDF files
#' rename_files(new_names = paste("file",1:3))
#' }
#' \dontshow{
#' dir <- tempdir()
#' require(lattice)
#' for(i in 1:3) {
#' pdf(file.path(dir, paste("plot", i, ".pdf", sep = "")))
#' print(xyplot(iris[,1] ~ iris[,i], data = iris))
#' dev.off()
#' }
#' n <- length(Sys.glob(file.path(dir,"*.pdf")))
#' rename_files(input_directory = dir, new_names = paste("file",1:n))
#' }
#' @export
#' @importFrom tcltk tk_choose.dir
#' @importFrom assertthat assert_that
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
rename_files <- function(input_directory = NULL, new_names) {
  assertthat::assert_that(is.character(new_names))

  if(is.null(input_directory)){
    #Choose a folder interactively
    input_directory<- tcltk::tk_choose.dir(caption = "Select directory which contains PDF fies")
   }

  # list all the pdf files in the selected folder
  input_filepaths <- (Sys.glob(file.path(input_directory,"*.pdf")))

  # Take the filepath arguments and format them for use in a system command
  output_filepath <-  file.path(input_directory, paste(new_names,".pdf",  sep = ""))
  file.rename(input_filepaths, output_filepath)

}
