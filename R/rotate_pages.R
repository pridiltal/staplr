#' Rotate selected pages of a pdf file
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to rotate the given pages of
#' the seleted PDF files
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @param rotatepages a vector of page numbers to be rotated
#' @param page_rotation An integer value from the vector c(0, 90, 180, 270).
#' Each option sets the page orientation as follows:
#' north: 0, east: 90, south: 180, west: 270. Note that the orientation cannot be
#' cummulatively changed (eg. 90 (east) will always turn the page so the beginning
#' of the page is on the right side)
#' @inheritParams input_filepath
#' @inheritParams output_filepath
#' @inheritParams overwrite
#'
#' @return this function returns a PDF document with the
#' remaining pages
#' @author Priyanga Dilini Talagala
#' @examples
#' \dontrun{
#' # This command prompts the user to select the file interactively.
#' # Rotate page 2 and 6 to 90 degrees clockwise
#' rotate_pages(rotatepages = c(3,6), page_rotation = 90)
#' }
#'
#' \dontrun{
#' if (requireNamespace("lattice", quietly = TRUE)) {
#' dir <- tempdir()
#' for(i in 1:3) {
#' pdf(file.path(dir, paste("plot", i, ".pdf", sep = "")))
#' print(lattice::xyplot(iris[,1] ~ iris[,i], data = iris))
#' dev.off()
#' }
#' output_file <- file.path(dir, paste('Full_pdf.pdf',  sep = ""))
#' staple_pdf(input_directory = dir, output_file)
#' input_path <- file.path(dir, paste("Full_pdf.pdf",  sep = ""))
#' output_path <-  file.path(dir, paste("Rotated_pgs_pdf.pdf",  sep = ""))
#' rotate_pages(rotatepages = c(2,3), page_rotation = 90,  input_path, output_path)
#' }
#' }
#' @export
#' @import utils
#' @importFrom  stringr str_extract
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
rotate_pages <- function(rotatepages, page_rotation = c(0,90,180,270), input_filepath = NULL, output_filepath = NULL, overwrite = TRUE) {

  assertthat::assert_that(is.numeric(rotatepages))
  page_rotation <- match.arg(as.character(page_rotation), c(0, 90, 180, 270))


  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }
  if(is.null(output_filepath)){
    #Choose output file interactively
    output_filepath <-  tcltk::tclvalue(tcltk::tkgetSaveFile(filetypes = '{Pdf {.pdf}}'))
  }

  input_filepath <- normalizePath(input_filepath, mustWork = TRUE)
  output_filepath <- normalizePath(output_filepath, mustWork = FALSE)

  metadataTemp <- tempfile()

  # Construct a system command to pdftk to get number of pages
  system_command <- paste(pdftk_cmd(),
                          shQuote(input_filepath),
                          "dump_data",
                          "output",
                          shQuote(metadataTemp))

  system(command = system_command)

  page_length <- as.numeric(stringr::str_extract(grep( "NumberOfPages", paste0(readLines(metadataTemp)),
                                             value = TRUE), "\\d+$"))

  total <- 1:page_length

  # if all pages are being turned, just call rotate_pdf
  # this is inefficient. fix later.
  if(all(total %in% rotatepages )){
    rotate_pdf(page_rotation = page_rotation,
               input_filepath = input_filepath,
               output_filepath = output_filepath,
               overwrite = overwrite)
  } else{
    keep <- total[-rotatepages]
    degree_0 <- split(keep, cumsum(seq_along(keep) %in% (which(diff(keep)>1)+1)))
    index_0 <- as.vector(sapply(degree_0, function(x) x[[1]]))
    f<-function(x){paste(min(x),"-",max(x),sep = "")}
    degree_0 <-  as.vector(unlist(lapply(degree_0,f)))


    direction <- c("north", "east", "south", "west" )[match(page_rotation,c(0,90,180,270))]
    degree_x <- paste(rotatepages, direction, sep="")

    index <- c(index_0,rotatepages)
    rotate <-vector(class(degree_0), length(c(degree_0,degree_x)))
    rotate[index[order(index)] %in% rotatepages] <- degree_x
    rotate[!(index[order(index)] %in% rotatepages)] <- degree_0

    # Construct a system command to pdftk
    system_command <- paste(pdftk_cmd(), shQuote(input_filepath),
                            "cat",
                            paste(rotate,collapse=' '),
                            "output",
                            "{shQuote(output_filepath)}",
                            sep = " ")

    fileIO(input_filepath = input_filepath,
           output_filepath = output_filepath,
           overwrite = overwrite,
           system_command = system_command)
  }
}
