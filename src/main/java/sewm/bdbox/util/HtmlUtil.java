package sewm.bdbox.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mozilla.universalchardet.UniversalDetector;

public class HtmlUtil {
  private static Logger logger = LogManager.getLogger(HtmlUtil.class);

  // text, charset
//  public static Entry<String, String> decode(byte[] data, List<String> encodings) {
//    for (String encoding : encodings) {
//      try {
//        return new AbstractMap.SimpleEntry<String, String>(Charset
//            .forName(encoding).newDecoder()
//            .onMalformedInput(CodingErrorAction.REPORT)
//            .onUnmappableCharacter(CodingErrorAction.REPLACE)
//            .decode(ByteBuffer.wrap(data)).toString(), encoding);
//      } catch (Exception e) {
//        logger.warn("Try to use another encoding.");
//      }
//    }
//    return null;
//  }

  public static String pCharset(UniversalDetector detector, byte[] data) {
    // String str = new String(data);
    // String encoding = pCharset(str);
    // if (encoding != null) {
    // return encoding;
    // }
    // return "gb18030";
    String encoding;
    detector.reset();
    detector.handleData(data, 0, data.length);
    detector.dataEnd();
    encoding = detector.getDetectedCharset();

    return encoding != null ? encoding : "gb18030";
  }

  private static String pCharset(String data) {
    // (5)
    if (data.length() > 400)
      data = data.substring(0, 400);
    // logger.info(data);
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
        logger.info(item[0]);
        return item[0];
      }
    }
    return null;
  }

  public static String parseTitle(String data) {
    // data = data.toLowerCase();
    // int i = data.indexOf("<title>");
    // int j = data.indexOf("</title>", i);
    // if (j == -1 || i == -1)
    // return null;
    // String s = data.substring(i + 7, j);
    Document doc = Jsoup.parse(data);
    String str = doc.title();
    return str;
  }

  public static String parseContent(String data) {
    org.jsoup.nodes.Document doc = Jsoup.parse(data);
    String str = doc.text();
    return str;
  }
}
