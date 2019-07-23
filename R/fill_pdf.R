# https://stackoverflow.com/questions/51850775/how-to-decode-a-character-with-numeric-character-references-in-it/51850941#51850941
# modified to support surrogate pairs (eg emoji)
sub_decimal <- function(char){
  while(TRUE){
    # first get the character. might be in multipart
    utf <- stringr::str_extract(char, '(\\&\\#([0-9]+)\\;)+')
    if(is.na(utf)){
      break()
    }
    utf <- stringr::str_extract_all(utf,'[0-9]+')[[1]]
    char <- sub('(\\&\\#([0-9]+)\\;)+', intToUtf8(utf,allow_surrogate_pairs = TRUE), char)
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
        if(fields[i-1] == ''){
          fields[i-1] <-  name
        } else{
          fields[i-1] <- paste0(fields[i-1], '.', name)
        }
      } else if (grepl("^/V",fdfLines[i+1])){
        if(fields[i+1] == ''){
          fields[i+1] <-  name
        } else{
          fields[i+1] <-  paste0(fields[i+1], '.', name)
        }
      } else if(grepl("^/Kids \\[",fdfLines[i+1])){
        z <- i+4
        nest <-  1
        while(nest!=0){
          if(grepl("^/V",fdfLines[z])){
            # if a field is found, append the name of the root to the left
            # separated by a "."
            if(fields[z] == ''){
              fields[z] = name
            } else{
              fields[z] <- paste0(fields[z],".",name)
            }
          } else if(grepl("^>>\\]",fdfLines[z])){
            # if another nest stops, that means we are inside another root
            nest <-  nest - 1
          } else if(grepl("^/Kids \\[",fdfLines[z])){
            # every time a root closes reduce the nest. if you reach 0
            # it means its over
            nest <-  nest + 1
          }
          # go back one line in the file.
          z = z + 1
        }
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
  annotatedFDF <- data.frame(fdfLines,fields,stringsAsFactors = FALSE)
  annotatedFDF$fields <- gsub('\\(','(',x = annotatedFDF$fields,fixed = TRUE)
  annotatedFDF$fields <- gsub('\\)',')',x = annotatedFDF$fields,fixed = TRUE)

  annotatedFDF$raw[annotatedFDF$fields==''] <-
    iconv(annotatedFDF$fdfLines[annotatedFDF$fields==''],
          from = 'latin1',to='latin1',toRaw = TRUE)

  return(annotatedFDF)

}


# this is an internal function that edits an fdf string
# it also deals with creating the binary for the output file
fdfEdit <- function(fieldToFill,annotatedFDF){
  fdfLine = which(annotatedFDF$fields == fieldToFill$name)

  if(length(fdfLine)==0){
    stop('Field "',fieldToFill$name,'" could not be found in the pdf. You may be using the wrong fields object or forgot to set convert_field_names correctly.')
  }


  if(is.na(fieldToFill$value)){
    fieldToFill$value <- ''
  }
  if(fieldToFill$type %in%  c('Text','Choice')){
    # this is necesarry because FDF file uses () to mark the beginning and end of text fields
    # we need to escape them
    originalValue <- as.character(fieldToFill$value)

    # at this point this is only here for debugging. Can be removed with no consequence.
    fieldToFill$value <- gsub(x = fieldToFill$value, pattern = '\\',replacement = '\\\\', fixed = TRUE)
    fieldToFill$value <- gsub(x = fieldToFill$value, pattern = '(',replacement = '\\(',fixed = TRUE)
    fieldToFill$value <- gsub(x = fieldToFill$value, pattern = ')',replacement = '\\)', fixed = TRUE)

    annotatedFDF[annotatedFDF$fields == fieldToFill$name,'fdfLines'] <- paste0('/V (',fieldToFill$value,')')




    # need to manually change the escaped characters into UTF-8 encoding...
    # this is currently very inefficient. I might be missing an easier way
    # to encode this. at the very least, having consecutive non escape
    # characters group up should make this more efficient
    # need to evaluate how badly it impacts performance

    # i don't actually need to use iconv here for the escaped characters
    # those are there to make the code more transparent.
    needEscape = grepl('\\(|\\)|\\\\',originalValue)
    originalEncoding = Encoding(originalValue)
    if (originalEncoding=='unknown'){
      originalEncoding = ''
    }
    if(needEscape){
      utf16Value = unlist(lapply(strsplit(originalValue,'')[[1]],function(x){
        if(x == '('){
          out <- c(iconv('\\',from='UTF-8',"UTF-16BE",toRaw = TRUE)[[1]],
                   iconv('(',from='UTF-8',"UTF-8",toRaw = TRUE)[[1]])
        } else if(x == ')'){
          out <- c(iconv('\\',from='UTF-8',"UTF-16BE",toRaw = TRUE)[[1]],
                   iconv(')',from='UTF-8',"UTF-8",toRaw = TRUE)[[1]])
        } else if(x =='\\'){
          out <- c(iconv('\\',from='UTF-8',"UTF-16BE",toRaw = TRUE)[[1]],
                   iconv('\\',from='UTF-8',"UTF-8",toRaw = TRUE)[[1]])
        } else{
          out <- iconv(x,from=originalEncoding,"UTF-16BE",toRaw = TRUE)[[1]]
        }
      }))
    } else{
      # if no escape is needed, just convert everthing
      utf16Value <- iconv(fieldToFill$value,from=originalEncoding,
                          "UTF-16BE",toRaw = TRUE)[[1]]
    }




    annotatedFDF[['raw']][[which(annotatedFDF$fields == fieldToFill$name)]] <-
      c(iconv("/V (",from = 'latin1',to='latin1',toRaw = TRUE)[[1]], # the encapsulating part is encoded in latin1
        as.raw(c(254,255)), # this is fe ff, the byte order mark for UTF-16BE
        utf16Value, # the actual field is converted from UTF-8 to UTF-16
        iconv(")",from = 'latin1',to='latin1',toRaw = TRUE)[[1]]) # close with a final paranthesis


    fieldToFill$value <-  paste0('(',fieldToFill$value,')')
  } else if(fieldToFill$type == 'Button'){
    annotatedFDF[fdfLine,'fdfLines'] = paste0('/V /',fieldToFill$value)

    # just encode the button with the regular latin1
    annotatedFDF[['raw']][[fdfLine]] <-
      iconv(annotatedFDF[fdfLine,'fdfLines'],from='latin1',"latin1",toRaw = TRUE)[[1]]


  } else{
    # As far as I know there are no other field types but just in case
    warning("I don't know how to fill the field type \"",fieldToFill$type,
            '". Please notify the dev.')
  }
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
#' @param convert_field_names By default \code{pdftk} will encode certain characters
#' of the field names in plain text UTF-8 so if using a non-latin alphabet, your
#' field names might be illegible. Setting this to TRUE will turn the UFT-8 code into
#' characters. However this process it not guaranteed to be perfect as pdftk does
#' not differentiate between encoded text and regular text using escape characters.
#' If you have field names that intentionally include components that look like encoded characters
#' this will attempt to fix them. Use this option only when necessary. If TRUE,
#' remember to set it to TRUE when using \code{\link{set_fields}} as well.
#' @param encoding_warning If field names include strings that look like plain text UTF-8
#' codes, the function will return a warning by default, suggesting setting \code{convert_field_names} to code{TRUE}.
#' If \code{encoding_warning} is \code{FALSE}, these warnings will be silenced.
#' @inheritParams overwrite
#'
#' @export
#'
#' @examples
#' \dontrun{
#' pdfFile = system.file('testForm.pdf',package = 'staplr')
#' idenfity_form_fields(pdfFile, 'testOutput.pdf')
#' }
idenfity_form_fields <- function(input_filepath = NULL, output_filepath = NULL,
                                 overwrite = TRUE,convert_field_names = FALSE,
                                 encoding_warning = TRUE){
  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }
  if(is.null(output_filepath)){
    #Choose output file interactively
    output_filepath <-  tcltk::tclvalue(tcltk::tkgetSaveFile(filetypes = '{Pdf {.pdf}}'))
  }

  fields = get_fields(input_filepath,
                      convert_field_names = convert_field_names,
                      encoding_warning = encoding_warning)

  fields = lapply(fields,function(field){
    if(field$type == 'Text'){
      field$value = field$name
    }
    return(field)
  })

  set_fields(input_filepath = input_filepath,
             output_filepath = output_filepath,
             fields = fields,
             convert_field_names = convert_field_names,
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
#' characters. However this process it not guaranteed to be perfect as pdftk does
#' not differentiate between encoded text and regular text using escape characters.
#' If you have field names that intentionally include components that look like encoded characters
#' this will attempt to fix them. Use this option only when necessary. If TRUE,
#' remember to set it to TRUE when using \code{\link{set_fields}} as well.
#' @param encoding_warning If field names include strings that look like plain text UTF-8
#' codes, the function will return a warning by default, suggesting setting \code{convert_field_names} to code{TRUE}.
#' If \code{encoding_warning} is \code{FALSE}, these warnings will be silenced.
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
get_fields <- function(input_filepath = NULL, convert_field_names = FALSE, encoding_warning = TRUE){
  if(is.null(input_filepath)){
    #Choose the pdf file interactively
    input_filepath <- file.choose(new = FALSE)
  }

  input_filepath <- normalizePath(input_filepath,mustWork = TRUE)

  fieldsTemp <- tempfile()
  # generate the data field dump in a temporary file
  # theoratically, using dump_data_fields_utf8 can get rid of the need to use sub_demical
  # but this fails to process inputs containing stuff like emoji
  system_command <- paste(pdftk_cmd(),
                          shQuote(input_filepath),
                          'dump_data_fields','output',
                          shQuote(fieldsTemp))
  system(system_command)
  # here encoding isn't important because any unusual character is in numeric character references
  fields <- paste0(readLines(fieldsTemp,encoding = 'UTF-8'),
                   collapse = '\n')

  # https://stackoverflow.com/questions/5060076/convert-html-character-entity-encoding-in-r
  fields <- XML::xpathApply(XML::htmlParse(fields, asText=TRUE,encoding = "UTF-8"),
                            "//body//text()",
                            XML::xmlValue)[[1]]

  # fields <- stringr::str_replace_all(fields,'&lt;','<')
  # fields <- stringr::str_replace_all(fields,'&gt;','>')
  # fields <- stringr::str_replace_all(fields,'&quot;','"')
  # fields <- stringr::str_replace_all(fields,'&amp;','&')

  fields <- strsplit(fields, '---')[[1]][-1]

  # parse the fields

  badFields = c()

  fields <- lapply(fields,function(x){
    type <- stringr::str_extract(x,'(?<=FieldType: ).*?(?=\n|$)')
    name <- stringr::str_extract(x,'(?<=FieldName: ).*?(?=\n|$)')

    value <- stringr::str_extract_all(x,'(?<=FieldValue: ).*?(?=\n|$)')[[1]]
    # sometimes there are multiple field values. It is currently unclear why this happens
    # but the example file I have only created the extra fieldValue when there was
    # an entry.
    if(length(value)>1){
      if(all(value == '')){
        value = ''
      } else if(length(value[value!=''])==1){
        value <- value[value!='']
      } else{
        warning(paste(name, "field has >1 FieldValues. set_fields only accepts fields of length one"))
      }
    }
    if(length(value)==0){
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
    } else if(encoding_warning && name != encodeUTF8(name)){
      assign("badFields",c(badFields,name),envir = parent.frame(n = 2))
    }

    return(list(type = type,
                name = name,
                value = sub_decimal(value)))
  })

  if(length(badFields)>0){
    warning(paste('some fields seems to include plain text UTF-8. Setting convert_field_names = TRUE might help. These fields have problematic names: \n', paste(badFields,collapse=', ')))
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
get_fdf_lines <- function(input_filepath,
                          output_filepath = NULL,
                          encoding = 'latin1',...){
  if(is.null(output_filepath)){
    output_filepath <- tempfile()
  }
  system_command <- paste(pdftk_cmd(),
                          shQuote(input_filepath),
                          'generate_fdf','output',
                          shQuote(output_filepath))
  system(system_command)
  fdfLines <- suppressWarnings(readLines(output_filepath,encoding = encoding,skipNul = TRUE,...))
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
#' @param convert_field_names If you set convert_field_names when using \code{\link{get_fields}}
#' you should set this to TRUE as well so the fields can be matched correctly.
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
                      convert_field_names = FALSE){
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

  # fdf = paste(annotatedFDF$fdfLines,collapse='\n')
  newFDF <- tempfile()

  fields_to_fdf(input_filepath, newFDF, fields, convert_field_names)

  # f = file(newFDF,open = "w",encoding = encoding)
  # writeLines(paste0(annotatedFDF$fdfLines,collapse= '\n'), f,useBytes = FALSE)
  # close(f)
  # writeLines(paste0(annotatedFDF$fdfLines,collapse= '\n'), newFDF)

  fill_from_fdf(input_filepath, output_filepath, newFDF, overwrite)


}

# internal function to create a new FDF file based on the given fields
fields_to_fdf = function(input_filepath, fdf_filepath, fields, convert_field_names){
  fdfLines <- get_fdf_lines(input_filepath)
  annotatedFDF = fdfAnnotate(fdfLines)
  if(convert_field_names){
    annotatedFDF$fields <- sapply(annotatedFDF$fields,encodeUTF8)
  }
  for(i in seq_along(fields)){
    fieldToFill <- fields[[i]]
    annotatedFDF <- fdfEdit(fieldToFill,annotatedFDF)
  }

  # add the line endings to the end of files
  annotatedFDF$raw = lapply(annotatedFDF$raw,function(x){
    c(x,as.raw(10))
  })
  # combine it all
  fdfRaw = do.call(c,annotatedFDF$raw)

  writeBin(fdfRaw,con = fdf_filepath)
}

# internal function to take in an FDF-PDF pair to return the filled output
fill_from_fdf = function(input_filepath, output_filepath, fdf_filepath, overwrite = TRUE){

  system_command <-
    paste(pdftk_cmd(),
          shQuote(input_filepath),
          "fill_form",
          shQuote(fdf_filepath),
          "output",
          "{shQuote(output_filepath)}",
          "need_appearances")


  fileIO(input_filepath = input_filepath,
         output_filepath = output_filepath,
         overwrite = overwrite,
         system_command = system_command)
}
