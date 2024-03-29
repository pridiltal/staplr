
#' file io
#'
#' @keywords internal
#' @name input_filepath
#'
#' @param input_filepath the path of the input PDF file. The default is set to
#'   NULL. IF NULL, it  prompt the user to select the folder interactively.
NULL

#' file io
#'
#' @keywords internal
#' @name output_filepath
#'
#' @param output_filepath the path of the output PDF file. The default is set to
#'   NULL. IF NULL, it  prompt the user to select the folder interactively.
NULL


#' Overwrite
#'
#' @keywords internal
#' @name overwrite
#'
#' @param overwrite If a file exists in \code{output_filepath}, should it be overwritten.
NULL

#' Return
#'
#' @keywords internal
#' @name return
#' @return TRUE if the operation was succesfful. FALSE if the operation fails.


fileIO <- function(input_filepath,
                  output_filepath,
                  overwrite,
                  system_command){


  if(!overwrite & file.exists(output_filepath)){
    stop(paste(output_filepath,'already exists. Set overwrite = TRUE to overwrite'))
  }
  if(output_filepath %in% input_filepath){
    true_out_path = output_filepath
    output_filepath <- tempfile()
    collision <- TRUE
  } else{
    collision <- FALSE
  }

  sys_out <- system(glue::glue(system_command))

  if(collision){
    # this last overwrite is redundant but just in case...
    file.copy(output_filepath,true_out_path,overwrite = overwrite)
  }

  if(sys_out == 0){
    return(TRUE)
  } else{
    return(FALSE)
  }

}




pdftk_cmd <- function(){
  custom_pdftk = getOption('staplr_custom_pdftk')
  if(!is.null(custom_pdftk)){
    return(custom_pdftk)
  }



  jOptions = getOption('staplr_java_options')
  if(is.null(jOptions)){
    jOptions = ''
  }
  path <- system.file('pdftk-java/pdftk.jar',package = 'staplr',mustWork = TRUE)
  javaPath <- rJava::.jcall( 'java/lang/System', 'S', 'getProperty', 'java.home' )
  javaFiles <- list.files(javaPath,recursive = TRUE,full.names = TRUE)
  java <- javaFiles[grepl('/java($|\\.exe)',javaFiles)]
  pdftk <- glue::glue('{shQuote(java)} {jOptions} -jar {shQuote(path)}')
  return(pdftk)
}
