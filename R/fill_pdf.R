# this function can be replaced by stringr::str_extract with minimal work
# but is added like this not to add a dependency
strExtract = function(x,regex){
  match = gregexpr(regex,x, perl = TRUE)
  if(match[[1]][1]!=-1){
    return(regmatches(x,gregexpr(regex,x, perl = TRUE))[[1]])
  } else{
    return(character(0))
  }
}

# this is an internal function that edits an fdf string
# fdfEdit = function(x, field,fdf){
#   if(x == TRUE & is.logical(x)){
#     x = '/Yes'
#   } else if (x == FALSE & is.logical(x)){
#     x = '/Off'
#   } else {
#     x %<>% gsub(x = ., pattern = '(',replacement = '\\\\(',fixed = TRUE) %>%
#       gsub(x = ., pattern = ')',replacement = '\\\\)', fixed = TRUE)
#     x = paste0('(',x,')')
#   }
#
#   fdf = stringr::str_replace(string  = fdf,pattern = paste0('/V\\s.*\n/T\\s\\(',field,'\\)'),
#                              replacement = paste0('/V ',x,'\n/T \\(',field,'\\)'))
#   return(fdf)
# }


#' Get form fields from a pdf
#'
#' @param input_filepath the path of the input PDF file.
#' The default is set to NULL. IF NULL, it  prompt the user to
#' select the folder interactively.
#'
#' @return A list of fields.
#' @export
#'
#' @examples
getFields <- function(input_filepath = NULL){
  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }

  fieldsTemp <- tempfile()
  on.exit(file.remove(fieldsTemp))

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

  names(fields) = sapply(fields,function(x){x$name})

  return(fields)
}



setFields = function(input_filepath = NULL, output_filepath = NULL, fields){
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
  on.exit(file.remove(tempFDF))

  fdf <- paste(readLines(tempFDF),
              collapse= '\n')


  for(i in seq_along(fields)){
    fieldToFill <- fields[[i]]
    fdf <- fdfEdit(fieldToFill,fdf)
  }

  newFDF <- tempfile()
  writeLines(fdf, newFDF)
  on.exit(file.remove(newFDF), add = TRUE)

  system_command <- paste('pdftk',
                          shQuote(input_filepath),
                          'fill_form', shQuote(newFDF),
                          'output', shQuote(output_filepath))
}
