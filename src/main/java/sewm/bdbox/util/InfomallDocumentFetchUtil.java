package sewm.bdbox.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Logger;
import org.tukaani.xz.SeekableFileInputStream;
import org.tukaani.xz.SeekableInputStream;

import sewm.bdbox.search.InfomallDocument;
import sewm.bdbox.search.InfomallDocumentIterator;

public class InfomallDocumentFetchUtil {
  private static final Logger logger = LogUtil
      .getLogger(InfomallDocumentFetchUtil.class);

  public static InfomallDocument fetch(String infomallDataRoot, String filename,
      String position) {
    return fetch(infomallDataRoot, filename, Long.parseLong(position));
  }

  public static InfomallDocument fetch(String infomallDataRoot, String filename,
      long position) {
    String dir = filename.split("\\.", 3)[1];
    Path path = Paths.get(infomallDataRoot).resolve(dir).resolve(filename);
    try (SeekableInputStream is = new SeekableFileInputStream(
        new File(path.toUri()))) {
      is.seek(position);
      InfomallDocumentIterator iter = new InfomallDocumentIterator(is,
          filename);
      return iter.next();
    } catch (IOException e) {
      LogUtil.error(logger, e);
      return null;
    }
  }
}
