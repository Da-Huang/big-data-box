package sewm.bdbox.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sewm.bdbox.search.InfomallDocument;

public class HtmlUtil {
  private static Logger logger = LogManager.getLogger(HtmlUtil.class);

  public static String pCharset(String data) {
    if (data.length() > 200)
      data = data.substring(0, 200);
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
      //logger.info(item[0]);
      if (item[0].isEmpty()) {
        return "utf8";
      } else if (item[0].subSequence(0, 2).equals("gb"))
        return "gb18030";
      else
        return "utf8";
    }
    return "utf8";
  }
}
