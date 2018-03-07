strExtract = function(x,regex){
  match = gregexpr(regex,x, perl = TRUE)
  if(match[[1]][1]!=-1){
    return(regmatches(x,gregexpr(regex,x, perl = TRUE))[[1]])
  } else{
    return(character(0))
  }
}



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
  fieldsTemp <- 'tempFDF'

  # generate the data field dump in a temporary file
  system_command <- paste('pdftk',
                          shQuote(input_filepath),
                          'dump_data_fields','output',
                          shQuote(fieldsTemp))
  system(system_command)

  fields <- paste0(readLines(fieldsTemp),
                   collapse = '\n')
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



setFields = function(pdf, input_filepath, ){


}
