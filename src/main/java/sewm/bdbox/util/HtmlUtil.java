package sewm.bdbox.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mozilla.universalchardet.UniversalDetector;

public class HtmlUtil {
  private static Logger logger = LogManager.getLogger(HtmlUtil.class);

  public static String pCharset(UniversalDetector detector, byte[] data) {
    String encoding;
    detector.reset();
    detector.handleData(data, 0, data.length);
    detector.dataEnd();
    encoding = detector.getDetectedCharset();
    return encoding != null ? encoding : "gb18030";
  }

  private static String pCharset(String data) {
    if (data.length() > 400)
      data = data.substring(0, 400);
    data = data.toLowerCase();
    while (true) {
      int i = data.indexOf("<meta");
      int j = data.indexOf(">", i);
      if (j == -1 || i == -1)
        break;

      String s = data.substring(i, j);
      data = data.substring(j);
      i = s.indexOf("charset=");
      if (i == -1)
        continue;
      s = s.substring(i + 8);
      if (s.isEmpty())
        continue;
      while (s.subSequence(0, 1).equals(" ")
          || s.subSequence(0, 1).equals("\"")) {
        s = s.substring(1);
        if (s.isEmpty())
          break;
      }

      String item[] = s.split(" |;|'|\"", 2);
      // logger.info(item[0]);
      if (item[0].isEmpty()) {
        return "gb18030";
      } else if (item[0].subSequence(0, 2).equals("gb"))
        return "gb18030";
      else {

        return item[0];
      }
    }
    return null;
  }

  public static String parseTitle(String data) {
    Document doc = Jsoup.parse(data);
    String str = doc.title();
    return str;
  }

  public static String parseContent(String data) {
    int i = data.indexOf("html");
    data = data.substring(i + 5);
    org.jsoup.nodes.Document doc = Jsoup.parse(data);
    String str = doc.text();
    return str;
  }

  public static String parseHost(String url) {
    URL aUrl;
    try {
      aUrl = new URL(url);
      String host = aUrl.getHost();
      return host;
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      logger.error(ExceptionUtil.getStacktraceString(e));
      return null;
    }
  }
}
