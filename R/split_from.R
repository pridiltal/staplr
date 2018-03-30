#' Splits single input PDF document into parts from given points
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to Split a single input PDF document
#' into two parts from a given point
#'
#' See the reference for detailed usage of \code{pdftk}.
#' @param pg_num A vector of non-negative integers. Split the pdf document into parts from the numbered pages.
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

  assertthat::assert_that(is.numeric(pg_num))

  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }

  if(is.null(output_directory)){
    #Select a folder to store output
    output_directory<- tcltk::tk_choose.dir(caption = "Select directory to save output")
  }

  parts <- length(pg_num) + 1
  splitPoints = c(0,pg_num,'end')

  for (i in seq_len(parts)){
    output_filepath <- file.path(output_directory, paste(prefix,i,".pdf",  sep = ""))

    system_command <- paste("pdftk",
                            shQuote(input_filepath),
                            "cat",
                            paste(as.integer(splitPoints[i])+1,'-',splitPoints[i+1], sep = ""),
                            "output",
                            shQuote(output_filepath),
                            sep = " ")

    system(command = system_command)
  }

}



