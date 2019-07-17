import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import com.gitlab.pdftk_java.pdftk;

public class AttachFilesTest extends BlackBox {
  @Test
  public void no_attachment() {
    pdftk("test/files/blank.pdf", "unpack_files", "output", tmpDirectory.getRoot().getPath());
  }

  @Test
  public void attach_one_file() throws IOException {
    String output = tmpDirectory.getRoot().getPath()+"/output.pdf";
    pdftk("test/files/blank.pdf", "attach_files",
          "test/files/blank.tex", "output", output);
    pdftk(output, "unpack_files", "output",
          tmpDirectory.getRoot().getPath());
    String expectedData = slurp("test/files/blank.tex");
    String attachedData = slurp(tmpDirectory.getRoot().getPath()+"/blank.tex");
    assertEquals(expectedData, attachedData);
  }

  @Test
  public void same_file_twice() throws IOException {
    String output = tmpDirectory.getRoot().getPath()+"/output.pdf";
    String output2 = tmpDirectory.getRoot().getPath()+"/output2.pdf";
    pdftk("test/files/blank.pdf", "attach_files", "test/files/blank.tex",
          "output", output);
    pdftk(output, "attach_files", "test/files/blank.tex", "output", output2);
    pdftk(output2, "unpack_files", "output", tmpDirectory.getRoot().getPath());
    String expectedData = slurp("test/files/blank.tex");
    String attachedData = slurp(tmpDirectory.getRoot().getPath()+"/blank.tex");
    assertEquals(expectedData, attachedData);
  }
};
