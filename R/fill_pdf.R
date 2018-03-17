# this function can be replaced by stringr::str_extract with minimal work
# but is added like this not to add a dependency
strExtract <- function(x,regex){
  match = gregexpr(regex,x, perl = TRUE)
  if(match[[1]][1]!=-1){
    return(regmatches(x,gregexpr(regex,x, perl = TRUE))[[1]])
  } else{
    return(character(0))
  }
}

# this is an internal function that edits an fdf string
fdfEdit <- function(fieldToFill,fdf){
  if(fieldToFill$type == 'Text'){
    # this is necesarry because FDF file uses () to mark the beginning and end of text fields
    # we need to escape them
    fieldToFill$value <- gsub(x = fieldToFill$value, pattern = '(',replacement = '\\\\(',fixed = TRUE)
    fieldToFill$value <- gsub(x = fieldToFill$value, pattern = ')',replacement = '\\\\)', fixed = TRUE)
    fieldToFill$value = paste0('(',fieldToFill$value,')')
  } else if(fieldToFill$type == 'Button'){
    fieldToFill$value = paste0('/',fieldToFill$value)
  } else{
    # As far as I knot there are no other field types but just in case
    warning("I don't know how to fill the field type \"",fieldToFill$type,
            '". Please notify the dev.')
  }

  # place the field in the correct location
  fdf <- stringi::stri_replace_first(str = fdf, regex = paste0('/V\\s.*\n/T\\s\\(',fieldToFill$name,'\\)'),
              replacement = paste0('/V ',fieldToFill$value,'\n/T \\(',fieldToFill$name,'\\)'))
  return(fdf)
}


#' Get form fields from a pdf
#'
#' @param input_filepath the path of the input PDF file.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#'
#' @return A list of fields. With type, name and value components. To use with
#' \code{\link{set_fields}} edit the value section of the fields you want to modify.
#' If the field is a button, the value will be a factor. In this case the factor
#' levels describe the possible values for the field.
#' @export
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
  file.remove(fieldsTemp)
  fields <- strsplit(fields, '---')[[1]][-1]

  fields <- lapply(fields,function(x){
    type <- strExtract(x,'(?<=FieldType: ).*?(?=\n)')
    name <- strExtract(x,'(?<=FieldName: ).*?(?=\n)')
    value <- strExtract(x,'(?<=FieldValue: ).*?(?=\n)')
    stateOptions <- strExtract(x,'(?<=FieldStateOption: ).*?(?=\n)')

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



set_fields = function(input_filepath = NULL, output_filepath = NULL, fields){
  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }
  if(is.null(output_filepath)){
    #Choose output file interactively
    input_filepath <-  tclvalue(tcltk::tkgetSaveFile())
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
    print(fdf %>% stringr::str_split('\n') %>% {.[[1]]} %>% length)
    fieldToFill <- fields[[i]]
    fdf <- fdfEdit(fieldToFill,fdf)
    print(fieldToFill$name)
  }

  newFDF <- tempfile()
  writeLines(fdf, newFDF)

  system_command <- paste('pdftk',
                          shQuote(input_filepath),
                          'fill_form', shQuote(newFDF),
                          'output', shQuote(output_filepath))
  system(system_command)
}
