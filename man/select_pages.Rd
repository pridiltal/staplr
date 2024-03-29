% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/select_pages.R
\name{select_pages}
\alias{select_pages}
\title{Select pages from a file}
\usage{
select_pages(
  selpages,
  input_filepath = NULL,
  output_filepath = NULL,
  overwrite = TRUE
)
}
\arguments{
\item{selpages}{a vector of page numbers to be selected}

\item{input_filepath}{the path of the input PDF file. The default is set to
NULL. IF NULL, it  prompt the user to select the folder interactively.}

\item{output_filepath}{the path of the output PDF file. The default is set to
NULL. IF NULL, it  prompt the user to select the folder interactively.}

\item{overwrite}{If a file exists in \code{output_filepath}, should it be overwritten.}
}
\value{
TRUE if the operation was succesfful. FALSE if the operation fails.
}
\description{
If the toolkit Pdftk is available in the
system, it will be called to combine the selected pages
in a new pdf file.

See the reference for detailed usage of \code{pdftk}.
}
\examples{

if (requireNamespace("lattice", quietly = TRUE)) {
dir <- tempfile()
dir.create(dir)
for(i in 1:3) {
pdf(file.path(dir, paste("plot", i, ".pdf", sep = "")))
print(lattice::xyplot(iris[,1] ~ iris[,i], data = iris))
dev.off()
}
output_file <- file.path(dir, paste('Full_pdf.pdf',  sep = ""))
staple_pdf(input_directory = dir, output_filepath = output_file)
input_path <- file.path(dir, paste("Full_pdf.pdf",  sep = ""))
output_path <-  file.path(dir, paste("trimmed_pdf.pdf",  sep = ""))
select_pages(selpages = 1, input_path, output_path)
}
}
\references{
\url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
}
\author{
Granville Matheson, Priyanga Dilini Talagala
}
