package sewm.bdbox.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.Logger;
import org.mozilla.universalchardet.UniversalDetector;
import org.tukaani.xz.SeekableFileInputStream;
import org.tukaani.xz.SeekableInputStream;

import sewm.bdbox.util.CommandlineUtil;
import sewm.bdbox.util.HtmlUtil;
import sewm.bdbox.util.JZlipUtil;
import sewm.bdbox.util.LogUtil;
import sewm.bdbox.util.StreamUtil;

/**
 * This class is not thread-safe.
 */
public class InfomallDocumentIterator {
  private static final Logger logger = LogUtil
      .getLogger(InfomallDocumentIterator.class);
  private SeekableInputStream is;
  private String filename;
  private UniversalDetector detector = new UniversalDetector(null);

  public InfomallDocumentIterator(SeekableInputStream is, String filename) {
    this.is = is;
    this.filename = filename;
  }

  public InfomallDocument next() {
    try {
      Long position = is.position();
      String version = null;
      String url = null;
      Date date = null;
      Integer unzipLength = null;
      Integer length = null;
      boolean hasData = false;

      String ip = null;
      String origin = null;
      String referer = null;

      String line;
      while ((line = StreamUtil.readLine(is)) != null && !line.isEmpty()) {
        hasData = true;
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
          date = parseDate(item[1]);
        } else if (item[0].equals("unzip-length")) {
          unzipLength = new Integer(item[1]);
        } else if (item[0].equals("length")) {
          length = new Integer(item[1]);
        } else if (item[0].equals("ip")) {
          ip = item[1];
        } else if (item[0].equals("origin")) {
          origin = item[1];
        } else if (item[0].equals("referer")) {
          referer = item[1];
        } else if (item[0].equals("anchortext")) {
          // Skip.
        } else {
          logger.warn("Type wrong\n");
        }
      }

      if (!hasData) {
        return null;
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
      String host = HtmlUtil.parseHost(url);
      if (host == null) {
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
      if (unzipBytes == null) {
        unzipBytes = new byte[0];
      }
      String charset = HtmlUtil.pCharset(detector, unzipBytes);
      String html = new String(unzipBytes, charset);

      String title = HtmlUtil.extractTitle(html);
      String cleanHtml = HtmlUtil.extractCleanHtml(html);
      String content = HtmlUtil.extractContentFromCleanHtml(cleanHtml);
      List<Entry<String, String>> anchors = HtmlUtil
          .extractAnchorsFromCleanHtml(cleanHtml, host, url);

      InfomallDocument doc = new InfomallDocument();
      doc.setFilename(filename);
      doc.setPosition(position);
      doc.setVersion(version);
      doc.setUrl(url);
      doc.setHost(host);
      doc.setDate(date);
      doc.setUnzipBytes(unzipBytes);
      doc.setHtml(html);
      doc.setCharset(charset);
      doc.setTitle(title);
      doc.setContent(content);
      doc.setIp(ip);
      doc.setOrigin(origin);
      doc.setReferer(referer);
      doc.setAnchors(anchors);
      return doc;
    } catch (IOException e) {
      LogUtil.error(logger, e);
      return null;
    }
  }

  public static Date parseDate(String dateStr) {
    final String pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
    DateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
    try {
      Date date = sdf.parse(dateStr);
      return date;
    } catch (Exception e) {
      LogUtil.error(logger, e);
    }
    return null;
  }

  public static void main(String[] args) {
    Options options = new Options();
    options.addOption(
        Option.builder().longOpt("help").desc("Print help message.").build());
    options.addOption(Option.builder().longOpt("data").argName("file").hasArg()
        .desc("Data path.").build());
    CommandLine line = CommandlineUtil.parse(options, args);

    LogUtil.check(logger, line.hasOption("data"), "Missing --data.");

    Path data = Paths.get(line.getOptionValue("data"));
    try (SeekableInputStream is = new SeekableFileInputStream(
        new File(data.toUri()))) {
      InfomallDocumentIterator iter = new InfomallDocumentIterator(is,
          data.getFileName().toString());
      InfomallDocument doc;
      int i = 0;
      while ((doc = iter.next()) != null) {
        logger.info(++i + ":" + doc.getPosition() + ": " + doc.getUrl());
        List<Entry<String, String>> anchors = doc.getAnchors();
        for (Entry<String, String> anchor : anchors) {
          logger.info(anchor);
          logger.info(HtmlUtil.normalizeURL(anchor.getValue()));
        }
      }
    } catch (IOException e) {
      LogUtil.error(logger, e);
    }

  }
}
