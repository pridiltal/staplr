
# this is an internal function that edits an fdf string
fdfEdit <- function(fieldToFill,fdf){
  if(is.na(fieldToFill$value)){
    fieldToFill$value=''
  }
  if(fieldToFill$type %in%  c('Text','Choice')){
    # this is necesarry because FDF file uses () to mark the beginning and end of text fields
    # we need to escape them
    fieldToFill$value <- gsub(x = fieldToFill$value, pattern = '\\',replacement = '\\\\\\\\', fixed = TRUE)
    fieldToFill$value <- gsub(x = fieldToFill$value, pattern = '(',replacement = '\\\\(',fixed = TRUE)
    fieldToFill$value <- gsub(x = fieldToFill$value, pattern = ')',replacement = '\\\\)', fixed = TRUE)
    fieldToFill$value = paste0('(',fieldToFill$value,')')
  } else if(fieldToFill$type == 'Button'){
    fieldToFill$value = paste0('/',fieldToFill$value)
  } else{
    # As far as I know there are no other field types but just in case
    warning("I don't know how to fill the field type \"",fieldToFill$type,
            '". Please notify the dev.')
  }

  # place the field in the correct location
  fdf <- stringr::str_replace(fdf,
                              paste0('/V\\s.*\n/T\\s\\(',fieldToFill$name,'\\)'),
                              paste0('/V ',fieldToFill$value,'\n/T \\(',fieldToFill$name,'\\)'))
  return(fdf)
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
      value <- factor(value,levels = stateOptions)
    }

    return(list(type = type,
                name = name,
                value = value))
  })

  names(fields) <- sapply(fields,function(x){x$name})

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

  tempFDF <- tempfile()
  # create the fdf file to fill
  system_command <- paste('pdftk',
                          shQuote(input_filepath),
                          'generate_fdf','output',
                          shQuote(tempFDF))
  system(system_command)


  fdf <- paste(readLines(tempFDF),
              collapse= '\n')

  for(i in seq_along(fields)){
    fieldToFill <- fields[[i]]
    fdf <- fdfEdit(fieldToFill,fdf)
  }

  newFDF <- tempfile()
  writeLines(fdf, newFDF)

  system_command <- paste('pdftk',
                          shQuote(input_filepath),
                          'fill_form', shQuote(newFDF),
                          'output', shQuote(output_filepath))
  system(system_command)
}
