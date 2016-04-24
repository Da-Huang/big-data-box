package sewm.bdbox.search;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.mozilla.universalchardet.UniversalDetector;
import org.tukaani.xz.SeekableFileInputStream;
import org.tukaani.xz.SeekableInputStream;

import sewm.bdbox.util.HtmlUtil;
import sewm.bdbox.util.JZlipUtil;
import sewm.bdbox.util.LogUtil;
import sewm.bdbox.util.StreamUtil;

public class InfomallDocumentIterator {
  private static Logger logger = LogUtil
      .getLogger(InfomallDocumentIterator.class);
  private SeekableInputStream is;
  private String path;
  private UniversalDetector detector = new UniversalDetector(null);

  public InfomallDocumentIterator(SeekableInputStream is, String file) {
    this.is = is;
    this.path = file;
  }

  public InfomallDocument next() {
    String version = null;
    String dateStr = null;
    String url = null;
    Date date = null;
    Integer unzipLength = null;
    Integer length = null;
    String data = null;
    Long position;
    try {
      position = is.position();
      String line;
      while ((line = StreamUtil.readLine(is)) != null && !line.isEmpty()) {
        String item[] = line.split(": ", 2);
        if (item.length != 2) {
          logger.warn("Length != 2");
          break;
        }
        if (item[0].equals("version")) {
          version = item[1];
        } else if (item[0].equals("url")) {
          url = item[1];
        } else if (item[0].equals("date")) {
          dateStr = item[1];
          date = parseDate(dateStr);
        } else if (item[0].equals("unzip-length")) {
          unzipLength = new Integer(item[1]);
        } else if (item[0].equals("length")) {
          length = new Integer(item[1]);
        } else {
          logger.warn("Type wrong\n");
        }
      }

      if (version == null) {
        logger.warn("Version not Found\n");
        return null;
      }
      if (url == null) {
        logger.warn("Url not Found\n");
        return null;
      }
      if (date == null) {
        logger.warn("Date not Found\n");
        return null;
      }
      if (unzipLength == null) {
        logger.warn("Unzip-length not Found\n");
        return null;
      }
      if (length == null) {
        logger.warn("Length not Found\n");
        return null;
      }
      byte[] bytes = new byte[length];
      int byteCount = is.read(bytes);
      if (byteCount < bytes.length) {
        logger.error("Data not get\n");
        return null;
      }
      StreamUtil.readLine(is);

      byte[] unzipBytes = JZlipUtil.decompress(bytes);
      data = new String(unzipBytes);

      String charset = HtmlUtil.pCharset(detector, unzipBytes);
      data = new String(unzipBytes, charset);

      String title = HtmlUtil.parseTitle(data);
      String content = HtmlUtil.parseContent(data);
      String host = HtmlUtil.parseHost(url);
      InfomallDocument doc = new InfomallDocument(version, url, date, data,
          position, charset, title, content, path, host);
      return doc;
    } catch (IOException e) {
      LogUtil.error(logger, e);
      return null;
    }
  }

  private static Date parseDate(String dateStr) {
    String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
    DateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
    try {
      Date date = sdf.parse(dateStr);
      return date;
    } catch (Exception e) {
      LogUtil.error(logger, e);
    }
    return null;
  }

  public static void main(String[] args) {
    try (SeekableInputStream is = new SeekableFileInputStream(
        new File("F:/U200201/Web_Raw.U200201.0001"))) {
      InfomallDocumentIterator iter = new InfomallDocumentIterator(is,
          "F:/U200201/Web_Raw.U200201.0001");
      InfomallDocument doc;
      int i = 0;
      while ((doc = iter.next()) != null) {
        logger.info(++i + ": " + doc.getUrl());
      }
    } catch (IOException e) {
      LogUtil.error(logger, e);
    }
  }
}
