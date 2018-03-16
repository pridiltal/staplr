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

  system_command <- paste('pdftk',
                          shQuote(input_filepath),
                          'dump_data_fields','output',
                          shQuote(fieldsTemp))



}
