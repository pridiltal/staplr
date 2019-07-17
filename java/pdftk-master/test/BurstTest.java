import org.junit.Test;

import java.io.IOException;

import com.gitlab.pdftk_java.pdftk;

public class BurstTest extends BlackBox {
  @Test
  public void burst_issue18() throws IOException {
    String pattern = tmpDirectory.getRoot().getPath() + "/page%04d.pdf";
    pdftk("test/files/issue18.pdf", "burst", "output", pattern);
  }
};
