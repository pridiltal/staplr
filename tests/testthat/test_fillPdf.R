context('test fill pdf')

test_that('fill pdf',{

  tempFile = tempfile(fileext = '.pdf')

  pdfFile = system.file('testForm.pdf',package = 'staplr')
  fields = get_fields(pdfFile)

  fields$TextField1$value = 'this is text'
  fields$TextField2$value = 'more text'
  fields$RadioGroup$value = 2
  fields$checkBox$value = 'Yes'

  set_fields(pdfFile,tempFile,fields)

  expect_true(file.exists(tempFile))
})
