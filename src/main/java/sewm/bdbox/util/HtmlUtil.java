package sewm.bdbox.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.Logger;
import org.mozilla.universalchardet.UniversalDetector;

public class HtmlUtil {
  private static final Logger logger = LogUtil.getLogger(HtmlUtil.class);

  public static String pCharset(UniversalDetector detector, byte[] data) {
    String encoding;
    detector.reset();
    detector.handleData(data, 0, data.length);
    detector.dataEnd();
    encoding = detector.getDetectedCharset();
    if (encoding == null || encoding.toLowerCase().contains("gb")) {
      return "gb18030";
    }
    return encoding;
  }

  @SuppressWarnings("unused")
  @Deprecated
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
    Pattern pattern =
        Pattern.compile("<title\\s*>(?<title>.*?)</title>", Pattern.DOTALL
            | Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(data);
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
        sb.append(" ");
      }
      sb.append(matcher.group("title"));
    }
    return sb.toString();
  }

  public static String parseContent(String data) {
    data = data.replaceAll("(?is)^.*?<html", "<html");
    data = data.replaceAll("(?is)<head.*?>.*?</head>", " ");
    data = data.replaceAll("(?is)<script.*?>.*?</script>", " ");
    data = data.replaceAll("(?is)<style.*?>.*?</style>", " ");
    data = data.replaceAll("(?s)<\\w+?.*?>", " ");
    data = data.replaceAll("</\\w+?>", " ");
    data = data.replaceAll("(?s)<!.*?>", " ");
    data = data.replaceAll("\\s+", " ");
    return data;
  }

  public static String parseHost(String url) {
    URL aUrl;
    try {
      aUrl = new URL(url);
      String host = aUrl.getHost();
      return host;
    } catch (MalformedURLException e) {
      LogUtil.error(logger, e);
      return null;
    }
  }

  public static void main(String[] args) {
    Options options = new Options();
    options.addOption(Option.builder().longOpt("help")
        .desc("Print help message.").build());
    options.addOption(Option.builder().longOpt("file").argName("file").hasArg()
        .desc("Data path.").build());
    CommandLine line = CommandlineUtil.parse(options, args);

    LogUtil.check(logger, line.hasOption("file"), "Missing --file.");

    try {
      String data =
          new String(Files.readAllBytes(Paths.get(line.getOptionValue("file"))));
      System.out.println(parseContent(data));
    } catch (IOException e) {
      LogUtil.error(logger, e);
    }
  }
}
