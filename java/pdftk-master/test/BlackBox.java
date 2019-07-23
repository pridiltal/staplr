import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.contrib.java.lang.system.TextFromStandardInputStream;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.lang.Process;
import java.lang.Runtime;

import com.gitlab.pdftk_java.pdftk;

// Utilities to write black-box tests, that is we provide a mock
// command line and look at the output.
public class BlackBox {
  @Rule
  public final SystemOutRule systemOut =
    new SystemOutRule().enableLog().muteForSuccessfulTests();

  @Rule
  public final TextFromStandardInputStream systemIn
    = TextFromStandardInputStream.emptyStandardInputStream();

  @Rule
  public final TemporaryFolder tmpDirectory = new TemporaryFolder();

  public String slurp(String filename) throws IOException {
    return new String(slurpBytes(filename));
  }
  public byte[] slurpBytes(String filename) throws IOException {
    return Files.readAllBytes(Paths.get(filename));
  }

  // Mock a command line call
  public void pdftk(String... args) {
    int error = pdftk.main_noexit(args);
    assertEquals(0, error);
  }

  // Capture the output of a command line call.
  //
  // TODO: Ideally this should already be handled by SystemOutRule,
  // but it seems that no more output is produced after one call to
  // pdftk() and SystemOutRule.clear() is not enough.
  public byte[] getPdf(String... args) {
    PrintStream orig = System.out;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
    pdftk(args);
    System.setOut(orig);
    return outputStream.toByteArray();
  }

  // For compatibility with Java < 9
  private byte[] readAllBytes(InputStream inputStream) throws IOException {
    final int bufferSize = 0x2000;
    byte[] buffer = new byte[bufferSize];
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    while (true) {
      int readBytes = inputStream.read(buffer, 0, bufferSize);
      if (readBytes < 0) break;
      outputStream.write(buffer, 0, readBytes);
    }
    return outputStream.toByteArray();
  }

  // Convert a PDF into a PS using pdftops from Poppler
  private byte[] pdfToPS(byte[] pdf) throws IOException {
    Process process = Runtime.getRuntime().exec(new String[]{"pdftops","-","-"});
    OutputStream pdfStream = process.getOutputStream();
    InputStream psStream = process.getInputStream();
    pdfStream.write(pdf);
    pdfStream.close();
    return readAllBytes(psStream);
  }

  // Compare two PDFs by checking that their PS representations are
  // equal. This ignores any differences in forms, links, bookmarks,
  // etc.
  //
  // Note that we cannot compare two PDFs byte-by-byte because they
  // have time-sensitive data, and even after removing that there can
  // be harmless differences such as objects being reordered or
  // renamed.
  public void assertPdfEqualsAsPS(byte[] pdf1, byte[] pdf2) {
    try {
      byte[] ps1 = pdfToPS(pdf1);
      byte[] ps2 = pdfToPS(pdf2);
      assertArrayEquals(ps1,ps2);
    }
    catch (IOException e) {
      fail("pdftops error");
    }
  }

};
