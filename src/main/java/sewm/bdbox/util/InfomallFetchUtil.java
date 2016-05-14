package sewm.bdbox.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.tukaani.xz.SeekableFileInputStream;
import org.tukaani.xz.SeekableInputStream;

import sewm.bdbox.search.InfomallDocument;
import sewm.bdbox.search.InfomallDocumentIterator;

public class InfomallFetchUtil {
  private static final Logger logger = LogUtil
      .getLogger(InfomallFetchUtil.class);

  public static Map<String, String> loadDataMap(Path path) {
    try {
      return Files.readAllLines(path).stream().collect(Collectors
          .toMap(line -> line.split("\t")[0], line -> line.split("\t")[1]));
    } catch (IOException e) {
      LogUtil.error(logger, e);
      return new HashMap<>();
    }
  }

  public static InfomallDocument fetch(final Map<String, String> dataMap,
      String filename, String position) {
    try {
      return fetch(dataMap, filename, Long.parseLong(position));
    } catch (Exception e) {
      LogUtil.error(logger, e);
      return null;
    }
  }

  public static InfomallDocument fetch(final Map<String, String> dataMap,
      String filename, long position) {
    String dir = filename.split("\\.", 3)[1];
    if (!dataMap.containsKey(dir)) {
      logger.warn("Directory " + dir + " is not contained in data map.");
      return null;
    }
    Path path = Paths.get(dataMap.get(dir)).resolve(dir).resolve(filename);
    return fetch(path, position);
  }

  public static InfomallDocument fetch(Path path, long position) {
    try (SeekableInputStream is = new SeekableFileInputStream(
        new File(path.toUri()))) {
      is.seek(position);
      InfomallDocumentIterator iter = new InfomallDocumentIterator(is,
          path.getFileName().toString());
      return iter.next();
    } catch (IOException e) {
      LogUtil.error(logger, e);
      return null;
    }
  }

  public static void main(String[] args) {
    InfomallDocument doc = fetch(
        Paths.get("/media/d/infomall/data0/U200201/Web_Raw.U200201.0009"),
        60641);
    System.out.println(doc.getHtml());
  }
}
