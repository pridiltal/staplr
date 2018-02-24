
staplr <img src="logo.png" align="right" height="150" />
========================================================

[![Project Status: Active  The project has reached a stable, usable state and is being actively developed.](http://www.repostatus.org/badges/latest/active.svg)](http://www.repostatus.org/#active) [![Licence](https://img.shields.io/badge/licence-GPL--3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.en.html) [![Build Status](https://travis-ci.org/pridiltal/staplr.svg?branch=master)](https://travis-ci.org/pridiltal/staplr)

[![Last-changedate](https://img.shields.io/badge/last%20change-2018--02--24-yellowgreen.svg)](/commits/master)

<!-- README.md is generated from README.Rmd. Please edit that file -->
staplr
======

This package provides function to manipulate PDF files:

    - merge multiple PDF files into one 
    - splits a single input PDF file into individual pages 
    - remove selected pages from a file
    - rename multiple files in a directory

This package is still under development and this repository contains a development version of the R package *staplr*.

Installation
------------

#### Install pdftk

download and install [pdftk](https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/) NB: this is not an R package!

#### Install staplr

You can install staplr from github with:

``` r
# install.packages("devtools")
devtools::install_github("pridiltal/staplr")
```

Example
-------

``` r
library(staplr)
# Merge multiple PDF files into one
staple_pdf()

# This command promts the user to select the file interactively. Remove page 2 and 3 from the selected file.
remove_pages(rmpages = c(2,3))

# This function splits a single input PDF document into individual pages
split_pdf()

# This function writes renamed files back to directory
#if the directory contains 3 PDF files
rename_files(new_names = paste("file",1:3))
```

References
----------

-   <https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/>
