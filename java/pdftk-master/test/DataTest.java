import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.io.IOException;

import com.gitlab.pdftk_java.pdftk;

public class DataTest extends BlackBox {
  @Test
  public void dump_data() throws IOException {
    pdftk("test/files/blank.pdf", "dump_data_utf8");
    String expectedData = slurp("test/files/blank.data");
    assertEquals(expectedData, systemOut.getLog());
  }

  @Test
  public void update_info_incomplete_record() {
    systemIn.provideLines("InfoBegin", "InfoKey: Title", " ","InfoBegin", "InfoKey: Author", " ");
    pdftk("test/files/blank.pdf", "update_info", "-", "output", "-");
  }
};
