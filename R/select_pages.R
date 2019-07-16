#' Select pages from a file
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to combine the selected pages
#' in a new pdf file.
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @param selpages a vector of page numbers to be selected
#' @inheritParams input_filepath
#' @inheritParams output_filepath
#' @inheritParams overwrite
#' @return this function returns a PDF document with the
#' remaining pages
#' @author Granville Matheson, Priyanga Dilini Talagala
#' @examples
#' \dontrun{
#' # This command prompts the user to select the file interactively.
#' # Select page 3 and 6 from the selected file.
#' select_pages(selpages = c(3,6))
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
#' input_path <- file.path(dir, paste("Full_pdf.pdf",  sep = ""))
#' output_path <-  file.path(dir, paste("trimmed_pdf.pdf",  sep = ""))
#' select_pages(selpages = 1, input_path, output_path)
#' }
#' @export
#' @import utils
#' @importFrom  stringr str_extract
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
select_pages <- function(selpages, input_filepath = NULL, output_filepath = NULL, overwrite = TRUE) {

  assertthat::assert_that(is.numeric(selpages))

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

  keep <- total[selpages]
  selected_pages <- split(keep, cumsum(seq_along(keep) %in%
                                         (which(diff(keep)>1)+1)))
  f<-function(x){paste(min(x),"-",max(x),sep = "")}
  selected_pages <- lapply(selected_pages,f)

  # Construct a system command to pdftk
  system_command <- paste(pdftk_cmd(),
                          shQuote(input_filepath),
                          "cat",
                          paste(unlist(selected_pages),collapse=" "),
                          "output",
                          "{shQuote(output_filepath)}",
                          sep = " ")
  # Invoke the command
  fileIO(input_filepath = input_filepath,
         output_filepath = output_filepath,
         overwrite = overwrite,
         system_command = system_command)}
