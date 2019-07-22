# this file is written in UTF-8 encoding


context('fill pdf internals')


test_that('fdf manipulation',{

  pdfFile <- system.file('simpleForm.pdf',package = 'staplr')

  pdfFileRich <- system.file('simpleFormRichText.pdf',package = 'staplr')

  tempFDF = tempfile()
  tempPDF = tempfile(fileext = '.pdf')
  fdfLines = get_fdf_lines(pdfFileRich, tempFDF)


  fill_from_fdf(input_filepath = pdfFile,fdf_filepath =  tempFDF,output_filepath = tempPDF,overwrite = TRUE)

  # pdftools interact wierdly with emoji, it also inconsistently sees the spaces between different
  # # characters.
  pdfTextNewRich = pdftools::pdf_text(tempPDF)
  expect_true(grepl('½¾ ‘’”“•', pdfTextNewRich))


  pdfFile =  system.file('testForm.pdf',package = 'staplr')
  fdfLines = get_fdf_lines(pdfFile,tempFDF)
  annotatedFDF = fdfAnnotate(fdfLines)

  reverseFDFLines = readLines('reverseFDF')
  reverseAnnotatedFDF = fdfAnnotate(reverseFDFLines)

  reverseExclusive = unique(reverseAnnotatedFDF$fields)[!unique(reverseAnnotatedFDF$fields) %in% unique(annotatedFDF$fields)]

  normalExclusive = unique(annotatedFDF$fields)[!unique(annotatedFDF$fields) %in% unique(reverseAnnotatedFDF$fields)]

  testthat::expect_length(reverseExclusive,0)
  testthat::expect_length(normalExclusive,0)



})
