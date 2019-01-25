# https://stackoverflow.com/questions/51850775/how-to-decode-a-character-with-numeric-character-references-in-it/51850941#51850941
sub_decimal <- function(char) {
  while(TRUE) {
    utf <- stringr::str_match(char, '\\&\\#([0-9]+)\\;')[,2]
    if (is.na(utf)) break()
    char <- sub('\\&\\#([0-9]+)\\;', intToUtf8(utf), char)
  }
  return(char)
}


fdfAnnotate <- function(fdfLines){
  fields <- vector(length = length(fdfLines),mode= "character")
  nests <- 0
  # iterate over every line
  for (i in seq_along(fdfLines)){
    if(grepl("^/T \\(",fdfLines[i])){
      # /T represents a field or a root name
      name <- stringr::str_extract(fdfLines[i],"(?<=\\().*?(?=\\))")
      if(grepl("^/V",fdfLines[i-1])){
        # if the line before the naming line starts with /V
        # there is no hierarhcy, just name the line
        fields[i-1] <-  name
      } else if(grepl("^>>\\]",fdfLines[i-1])){
        # if the line above the name is >>] the name represents a root
        # start reading from the line above
        z <- i-2
        # this keeps track of the nest levels.
        # we will be reading the file backwards trying to
        # reach to the end of this root
        nest <-  1
        while(nest!=0){
          if(grepl("^/V",fdfLines[z])){
            # if a field is found, append the name of the root to the left
            # separated by a "."
            fields[z] <- paste0(name,".",fields[z])
          } else if(grepl("^>>\\]",fdfLines[z])){
            # if another nest stops, that means we are inside another root
            nest <-  nest + 1
          } else if(grepl("^/Kids \\[",fdfLines[z])){
            # every time a root closes reduce the nest. if you reach 0
            # it means its over
            nest <-  nest - 1
          }
          # go back one line in the file.
          z = z - 1
        }
      }
    }
  }
  data.frame(fdfLines,fields,stringsAsFactors = FALSE)
}

# this is an internal function that edits an fdf string
fdfEdit <- function(fieldToFill,annotatedFDF){
  if(is.na(fieldToFill$value)){
    fieldToFill$value <- ''
  }
  if(fieldToFill$type %in%  c('Text','Choice')){
    # this is necesarry because FDF file uses () to mark the beginning and end of text fields
    # we need to escape them
    fieldToFill$value <- gsub(x = fieldToFill$value, pattern = '\\',replacement = '\\\\', fixed = TRUE)
    fieldToFill$value <- gsub(x = fieldToFill$value, pattern = '(',replacement = '\\(',fixed = TRUE)
    fieldToFill$value <- gsub(x = fieldToFill$value, pattern = ')',replacement = '\\)', fixed = TRUE)
    fieldToFill$value <-  paste0('(',fieldToFill$value,')')
  } else if(fieldToFill$type == 'Button'){
    fieldToFill$value <-  paste0('/',fieldToFill$value)
  } else{
    # As far as I know there are no other field types but just in case
    warning("I don't know how to fill the field type \"",fieldToFill$type,
            '". Please notify the dev.')
  }

  # place the field in the correct location
  annotatedFDF[annotatedFDF$fields == fieldToFill$name,'fdfLines'] = paste('/V',fieldToFill$value)

  return(annotatedFDF)
}


#' Get form fields from a pdf form
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to get form fields from a pdf file.
#'
#' See the reference for detailed usage of \code{pdftk}.
#'
#' @param input_filepath the path of the input PDF file. The default is set to
#'   NULL. IF NULL, it  prompt the user to select the folder interactively.
#'
#' @return A list of fields. With type, name and value components. To use with
#'   \code{\link{set_fields}} edit the value element of the fields you want to
#'   modify. If the field of type "button", the value will be a factor. In this
#'   case the factor levels describe the possible values for the field. For
#'   example for a checkbox the typical level names would be "Off" and "Yes",
#'   corresponding to non checked and checked states respectively.
#' @author Ogan Mancarci
#' @seealso \code{link{set_fields}}
#' @examples
#' \dontrun{
#' pdfFile = system.file('testForm.pdf',package = 'staplr')
#' fields = get_fields(pdfFile)
#' }
#' @export
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
get_fields <- function(input_filepath = NULL){
  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }

  input_filepath <- normalizePath(input_filepath,mustWork = TRUE)

  fieldsTemp <- tempfile()

  # generate the data field dump in a temporary file
  system_command <- paste('pdftk',
                          shQuote(input_filepath),
                          'dump_data_fields','output',
                          shQuote(fieldsTemp))
  system(system_command)

  fields <- paste0(readLines(fieldsTemp),
                   collapse = '\n')
  fields <- strsplit(fields, '---')[[1]][-1]

  # parse the fields
  fields <- lapply(fields,function(x){
    type <- stringr::str_extract(x,'(?<=FieldType: ).*?(?=\n|$)')
    name <- stringr::str_extract(x,'(?<=FieldName: ).*?(?=\n|$)')
    value <- stringr::str_extract(x,'(?<=FieldValue: ).*?(?=\n|$)')
    if(is.na(value)){
      # sometimes FieldValue is non populated
      # note the field is a button, this will cause it to be returned as an NA.
      # this is later handled by fdfEdit function which replaces the NA with
      # an empty string when filling the fdf file.
      value = ''
    }
    stateOptions <- stringr::str_extract_all(x,'(?<=FieldStateOption: ).*?(?=\n|$)')[[1]]

    if(length(stateOptions)>0){
      value <- factor(sub_decimal(value),levels = sapply(stateOptions,sub_decimal))
    }

    return(list(type = type,
                name = sub_decimal(name),
                value = value))
  })

  names(fields) <- sapply(fields,function(x){x$name})

  # remove typeless fields. it seems like nested hierarchies generate these typeless
  # fields that don't really exist and don't appear on the fdf file.
  fields = fields[sapply(fields,function(x){x$type})!='']

  return(fields)
}





#' Set fields of a pdf form
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to fill a pdf form with given a list of fields.
#' List of fields can be acquired by \code{\link{get_fields}} function.
#'
#' See the reference for detailed usage of \code{pdftk}.
#'
#' @param input_filepath the path of the input PDF file. The default is set to
#'   NULL. IF NULL, it  prompt the user to select the folder interactively.
#' @param output_filepath the path of the output PDF file. The default is set to
#'   NULL. IF NULL, it  prompt the user to select the folder interactively.
#' @param fields Fields returned from \code{\link{get_fields}} function. To make
#'   changes in a PDF, edit the \code{values} component of an element within
#'   this list
#'
#' @export
#' @author Ogan Mancarci
#' @seealso \code{\link{get_fields}}
#' @examples
#' \dontrun{
#' pdfFile = system.file('testForm.pdf',package = 'staplr')
#' fields = get_fields(pdfFile)
#'
#' fields$TextField1$value = 'this is text'
#' fields$TextField2$value = 'more text'
#' fields$RadioGroup$value = 2
#' fields$checkBox$value = 'Yes'
#'
#' set_fields(pdfFile,'filledPdf.pdf',fields)
#' }
#'
#' @references \url{https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/}
set_fields = function(input_filepath = NULL, output_filepath = NULL, fields){
  assertthat::assert_that(is.list(fields))

  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }
  if(is.null(output_filepath)){
    #Choose output file interactively
    output_filepath <-  tcltk::tclvalue(tcltk::tkgetSaveFile(filetypes = '{Pdf {.pdf}}'))
  }

  input_filepath <- normalizePath(input_filepath,mustWork = TRUE)
  output_filepath <- normalizePath(output_filepath,mustWork = FALSE)

  tempFDF <- tempfile()
  # create the fdf file to fill
  system_command <- paste('pdftk',
                          shQuote(input_filepath),
                          'generate_fdf','output',
                          shQuote(tempFDF))
  system(system_command)


  fdfLines <- readLines(tempFDF,encoding ='latin1')

  annotatedFDF = fdfAnnotate(fdfLines)

  assertthat::assert_that(all(names(fields) %in% annotatedFDF$fields),
                     msg = paste('Field names do not match the fields of the pdf.',
                                 'Either you are using fields generated from a wrong',
                                 'pdf file or there is a parsing error. If there is a',
                                 'parsing error, please notify the developers (https://github.com/pridiltal/staplr)'))


  for(i in seq_along(fields)){
    fieldToFill <- fields[[i]]
    annotatedFDF <- fdfEdit(fieldToFill,annotatedFDF)
  }

  # fdf = paste(annotatedFDF$fdfLines,collapse='\n')

  newFDF <- tempfile()
  f = file(newFDF,open = "w",encoding = 'latin1')
  writeLines(paste0(annotatedFDF$fdfLines,collapse= '\n'), f,useBytes = TRUE)
  close(f)

  system_command <- paste('pdftk',
                          shQuote(input_filepath),
                          'fill_form', shQuote(newFDF),
                          'output', shQuote(output_filepath))
  system(system_command)
}
