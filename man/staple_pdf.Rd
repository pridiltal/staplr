% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/staple_pdf.R
\name{staple_pdf}
\alias{staple_pdf}
\title{Merge multiple PDF files into one}
\usage{
staple_pdf(
  input_directory = NULL,
  input_files = NULL,
  output_filepath = NULL,
  overwrite = TRUE
)
}
\arguments{
\item{input_directory}{the path of the input PDF files.
The default is set to NULL. If NULL, it  prompt the user to
select the folder interactively.}

\item{input_files}{a vector of input PDF files. The default is set to NULL. If NULL and \code{input_directory} is also NULL, the user is propted to select a folder interactively.}

\item{output_filepath}{the path of the output PDF file. The default is set to
NULL. IF NULL, it  prompt the user to select the folder interactively.}

\item{overwrite}{If a file exists in \code{output_filepath}, should it be overwritten.}
}
\value{
TRUE if the operation was succesfful. FALSE if the operation fails.
}
\description{
If the toolkit Pdftk is available in the
system, it will be called to merge the PDF files.

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
}
}
\references{
\url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
}
\author{
Priyanga Dilini Talagala and Daniel Padfield
}
