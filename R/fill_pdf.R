# https://stackoverflow.com/questions/51850775/how-to-decode-a-character-with-numeric-character-references-in-it/51850941#51850941
sub_decimal <- function(char) {
  while(TRUE) {
    utf <- stringr::str_match(char, '\\&\\#([0-9]+)\\;')[,2]
    if (is.na(utf)) break()
    char <- sub('\\&\\#([0-9]+)\\;', intToUtf8(utf), char)
  }
  return(char)
}

encodeUTF8 <- function(char){
  while(TRUE){
    utf8 = stringr::str_extract(char, '(\\#([A-Z]|[0-9]){2})+')
    if(is.na(utf8)) break()
    utf8chars <- strsplit(utf8, "#")
    # https://stackoverflow.com/questions/55583644/convert-utf-8-encoding-in-text-form-to-characters
    # just grab the first entry, and leave off the blank
    utf8chars <- utf8chars[[1]][-1]
    # Convert the hex to integer
    utf8int <- strtoi(paste0("0x",utf8chars))
    # Then to raw
    utf8raw <- as.raw(utf8int)
    # And finally to character
    utf8char <- rawToChar(utf8raw)
    # On Windows you'll also need this
    Encoding(utf8char) <- "utf-8"

    char <- sub('(\\#([A-Z]|[0-9]){2})+',utf8char,char)
  }
  return(char)
}

# getting code review at https://codereview.stackexchange.com/questions/212118/a-faux-parser-for-fdf-files
fdfAnnotate <- function(fdfLines){
  fields <- vector(length = length(fdfLines),mode= "character")
  nests <- 0
  # iterate over every line
  for (i in seq_along(fdfLines)){
    if(grepl("^/T \\(",fdfLines[i])){
      # /T represents a field or a root name
      name <- stringr::str_extract(fdfLines[i],"(?<=/T \\().*?(?=\\)$)")
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
  annotatedFDF = data.frame(fdfLines,fields,stringsAsFactors = FALSE)
  annotatedFDF$fields <- gsub('\\(','(',x = annotatedFDF$fields,fixed = TRUE)
  annotatedFDF$fields <- gsub('\\)',')',x = annotatedFDF$fields,fixed = TRUE)
  return(annotatedFDF)

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



#' Identify text form fields
#'
#' Helps identification of text forum fields by creating a file that is filled
#' with field names. Some pdf editors show field names when you mouse over the
#' fields as well.
#'
#' @inheritParams input_filepath
#' @inheritParams output_filepath
#' @inheritParams overwrite
#' @export
#'
#' @examples
#' \dontrun{
#' pdfFile = system.file('testForm.pdf',package = 'staplr')
#' idenfity_form_fields(pdfFile, 'testOutput.pdf')
#' }
idenfity_form_fields <- function(input_filepath = NULL, output_filepath = NULL,overwrite = TRUE){
  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }
  if(is.null(output_filepath)){
    #Choose output file interactively
    output_filepath <-  tcltk::tclvalue(tcltk::tkgetSaveFile(filetypes = '{Pdf {.pdf}}'))
  }

  fields = get_fields(input_filepath)

  fields = lapply(fields,function(field){
    if(field$type == 'Text'){
      field$value = field$name
    }
    return(field)
  })

  set_fields(input_filepath = input_filepath,
             output_filepath = output_filepath,
             fields = fields,
             overwrite = overwrite)

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
#' @param convert_field_names By default \code{pdftk} will encode certain characters
#' of the field names in plain text UTF-8 so if using a non-latin alphabet, your
#' field names might be illegible. Setting this to TRUE will turn the UFT-8 code into
#' characters. However this process it not guaranteed to be perfect as pdftk doesn
#' not differentiate between encoded text and regular text using escape characters.
#' If you have field names that intentionally include components that look like encoded characters
#' this will attempt to fix them. Use this option only when necessary.
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
#'
get_fields <- function(input_filepath = NULL, convert_field_names = FALSE){
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
  # here encoding isn't important because any unusual character is in numeric character references
  fields <- paste0(readLines(fieldsTemp),
                   collapse = '\n')
  fields <- stringr::str_replace_all(fields,'&lt;','<')
  fields <- stringr::str_replace_all(fields,'&gt;','>')
  fields <- stringr::str_replace_all(fields,'&quot;','"')
  fields <- stringr::str_replace_all(fields,'&amp;','&')
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

    if(convert_field_names){
      name = encodeUTF8(name)
      attr(name,"converted") = TRUE
    }

    return(list(type = type,
                name = name,
                value = sub_decimal(value)))
  })

  if(convert_field_names){

  }

  names(fields) <- sapply(fields,function(x){x$name})

  # remove typeless fields. it seems like nested hierarchies generate these typeless
  # fields that don't really exist and don't appear on the fdf file.
  fields = fields[sapply(fields,function(x){x$type})!='']

  # remove fields that don't appear on the FDF
  fdfLines <- get_fdf_lines(input_filepath)
  annotatedFDF <- fdfAnnotate(fdfLines)
  if(convert_field_names){
    annotatedFDF$fields <- sapply(annotatedFDF$fields,encodeUTF8)
  }
  fields = fields[names(fields) %in% annotatedFDF$fields]

  # class(fields) = 'pdf_fields'


  return(fields)
}



# taken outside to make testing easier
# this file has always wierd characters at the beginning
# not reading with latin1 will result in a warning. and later on
# causes parsing failures regardless of the character set being used in
# the input pdf
get_fdf_lines <- function(input_filepath,encoding = 'latin1'){

  tempFDF <- tempfile()
  system_command <- paste('pdftk',
                          shQuote(input_filepath),
                          'generate_fdf','output',
                          shQuote(tempFDF))
  system(system_command)
  fdfLines <- suppressWarnings(readLines(tempFDF,encoding = encoding))
  return(fdfLines)
}


#' Set fields of a pdf form
#'
#' @description If the toolkit Pdftk is available in the
#' system, it will be called to fill a pdf form with given a list of fields.
#' List of fields can be acquired by \code{\link{get_fields}} function.
#'
#' See the reference for detailed usage of \code{pdftk}.
#'
#' @inheritParams input_filepath
#' @inheritParams output_filepath
#' @param fields Fields returned from \code{\link{get_fields}} function. To make
#'   changes in a PDF, edit the \code{values} component of an element within
#'   this list
#' @inheritParams overwrite
#' @param encoding Encoding option passed to \code{\link[base]{file}}.
#' Change this and \code{useByte} if characters you are trying to write to the
#' field are not writable to a file using the default options
#' @param useByte useByte option passed to \code{\link[base]{writeLines}}.
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
#'
set_fields = function(input_filepath = NULL, output_filepath = NULL, fields,
                      overwrite = TRUE,
                      encoding = "latin1",
                      useBytes = TRUE){
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

  fdfLines <- get_fdf_lines(input_filepath)

  annotatedFDF = fdfAnnotate(fdfLines)

  for(i in seq_along(fields)){
    fieldToFill <- fields[[i]]
    annotatedFDF <- fdfEdit(fieldToFill,annotatedFDF)
  }

  # fdf = paste(annotatedFDF$fdfLines,collapse='\n')

  newFDF <- tempfile()
  f = file(newFDF,open = "w",encoding = encoding)
  writeLines(paste0(annotatedFDF$fdfLines,collapse= '\n'), f,useBytes = useBytes)
  close(f)
  system_command <-
    paste("pdftk",
          shQuote(input_filepath),
          "fill_form",
          shQuote(newFDF),
          "output",
          "{shQuote(output_filepath)}")


  fileIO(input_filepath = input_filepath,
         output_filepath = output_filepath,
         overwrite = overwrite,
         system_command = system_command)
  }
