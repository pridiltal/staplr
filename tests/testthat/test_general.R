context('basic functionality')

test_that('fill_pdf',{

  tempFile = tempfile(fileext = '.pdf')

  pdfFile = system.file('testForm.pdf',package = 'staplr')
  fields = get_fields(pdfFile)

  fields$TextField1$value = 'this is text'
  fields$TextField2$value = 'more text'
  fields$RadioGroup$value = 2
  fields$checkBox$value = 'Yes'

  set_fields(pdfFile,tempFile,fields)

  # ensure that the resulting file is filled with the correct text
  expect_true(grepl('this is text', pdftools::pdf_text(tempFile)[1]))
})



test_that('remove_pages',{
  pdfFile = system.file('testForm.pdf',package = 'staplr')
  tempFile = tempfile(fileext = '.pdf')

  remove_pages(rmpages = 1, pdfFile, tempFile)
  # ensure that the page is removed so the new page 1 is the old page 2
  expect_true(pdftools::pdf_text(pdfFile)[2] == pdftools::pdf_text(tempFile)[1])
})

test_that('rotate',{
  pdfFile = system.file('testForm.pdf',package = 'staplr')
  tempFile = tempfile(fileext = '.pdf')
  rotate_pages(c(1,2), 90, pdfFile, tempFile)

  # check the dimensions of the rotated pdf files to see if its rotated
  newDims = dim(pdftools::pdf_render_page(tempFile,1))
  oldDims = dim(pdftools::pdf_render_page(pdfFile,1))
  expect_equal(newDims[2],oldDims[3])
  expect_equal(newDims[3],oldDims[2])


  tempFile = tempfile(fileext = '.pdf')
  rotate_pdf(90, pdfFile, tempFile)

  # check the dimensions of the rotated pdf files to see if its rotated
  newDims = dim(pdftools::pdf_render_page(tempFile,1))
  oldDims = dim(pdftools::pdf_render_page(pdfFile,1))
  expect_equal(newDims[2],oldDims[3])
  expect_equal(newDims[3],oldDims[2])
})


test_that('split',{
  pdfFile = system.file('testForm.pdf',package = 'staplr')
  pdfFileInfo = pdftools::pdf_info(pdfFile)
  tempDir = tempfile()
  dir.create(tempDir)
  split_pdf(pdfFile,tempDir,prefix = 'p')

  splitFiles = list.files(tempDir,pattern = '.pdf',full.names = TRUE)

  # expect as many pages as the number of pages in the original file
  expect_equal(length(splitFiles), pdfFileInfo$pages)

  # compare the second page of the original file with the second page created
  # this also checks if the prefix works and the number of trailing zeroes
  expect_equal(pdftools::pdf_text(pdfFile)[2],pdftools::pdf_text(file.path(tempDir,'p0002.pdf')))

  tempDir = tempfile()
  dir.create(tempDir)
  split_from(pg_num = 1,pdfFile,tempDir,prefix = 'p')
  # compare the text of the original file with the resulting files
  expect_equal(pdftools::pdf_text(pdfFile)[1],pdftools::pdf_text(file.path(tempDir,'p1.pdf')))
  expect_equal(pdftools::pdf_text(pdfFile)[2],pdftools::pdf_text(file.path(tempDir,'p2.pdf'))[1])
  expect_equal(pdftools::pdf_text(pdfFile)[3],pdftools::pdf_text(file.path(tempDir,'p2.pdf'))[2])


  # multi split points
  tempDir = tempfile()
  dir.create(tempDir)
  split_from(pg_num = c(1,2),pdfFile,tempDir,prefix = 'p')

  expect_equal(pdftools::pdf_text(pdfFile)[1],pdftools::pdf_text(file.path(tempDir,'p1.pdf')))
  expect_equal(pdftools::pdf_text(pdfFile)[2],pdftools::pdf_text(file.path(tempDir,'p2.pdf')))
  expect_equal(pdftools::pdf_text(pdfFile)[3],pdftools::pdf_text(file.path(tempDir,'p3.pdf')))

})


test_that('staple',{
  # create individual pdfs first
  pdfFile = system.file('testForm.pdf',package = 'staplr')
  pdfFileInfo = pdftools::pdf_info(pdfFile)
  tempDir = tempfile()
  dir.create(tempDir)
  split_pdf(pdfFile,tempDir)

  # re-create the original file
  tempFile = tempfile(fileext = '.pdf')
  staple_pdf(input_directory = tempDir,output_filepath = tempFile)
  # compare with original file
  expect_identical(pdftools::pdf_text(pdfFile) ,pdftools::pdf_text(tempFile))

  # staple by filename
  tempFile = tempfile(fileext = '.pdf')
  files = list.files(tempDir,pattern = '.pdf',full.names = TRUE)
  staple_pdf(input_files = files[c(1,2)],output_filepath = tempFile)
  expect_identical(pdftools::pdf_text(pdfFile)[1:2] ,pdftools::pdf_text(tempFile))

})
