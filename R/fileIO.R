
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


fileIO = function(input_filepath,
                  output_filepath,
                  overwrite,
                  system_command){


  if(!overwrite & file.exists(output_filepath)){
    stop(paste(output_filepath,'already exists. Set overwrite = TRUE to overwrite'))
  }
  if(input_filepath == output_filepath){
    true_out_path = output_filepath
    output_filepath = tempfile()
    collision <- TRUE
  } else{
    collision <- FALSE
  }

  system(glue::glue(system_command))

  if(collision){
    # this last overwrite is redundant but just in case...
    file.copy(output_filepath,true_out_path,overwrite = overwrite)
  }

  }
