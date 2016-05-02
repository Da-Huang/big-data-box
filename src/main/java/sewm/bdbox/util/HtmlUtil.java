package sewm.bdbox.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
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
        Pattern.compile("<title\\s*>(?<title>.*?)</title>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
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

  private static Pattern IMG_PATTERN =
      Pattern.compile("<img\\s.*?(alt=(?<text>'.*?'|\".*?\"))?.*?>",
          Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

  public static String parseContent(String data) {
    data = data.replaceAll("(?is)^.*?<html", "<html");
    data = data.replaceAll("(?is)<head.*?>.*?</head>", " ");
    data = data.replaceAll("(?is)<script.*?>.*?</script>", " ");
    data = data.replaceAll("(?is)<style.*?>.*?</style>", " ");

    Matcher imgMatcher = IMG_PATTERN.matcher(data);
    StringBuffer sb = new StringBuffer();
    while (imgMatcher.find()) {
      String replacement = imgMatcher.group("text");
      replacement =
          " " + replacement.substring(1, replacement.length() - 1) + " ";
      imgMatcher.appendReplacement(sb, replacement);
    }
    imgMatcher.appendTail(sb);
    data = sb.toString();

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

  private static Pattern A_PATTERN =
      Pattern.compile("<a(?<attr>.*?)>(?<aparse>.*?)</a>",
          Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  private static Pattern HREF_PATTERN =
      Pattern.compile("href=(?<url>'.*?'|\".*?\")",
          Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

  public static List<Entry<String, String>> parseURL(
      String data, String host, String url) {
    List<Entry<String, String>> ans = new ArrayList<Entry<String, String>>();
    Matcher matcher = A_PATTERN.matcher(data);

    while (matcher.find()) {
      String aparse = matcher.group("aparse");
      aparse = parseContent(aparse);
      Matcher matcher2 = HREF_PATTERN.matcher(matcher.group("attr"));
      if (matcher2.find()) {
        String urlString = matcher2.group("url");
        if (urlString.isEmpty())
          continue;
        urlString = urlString.substring(1, urlString.length() - 1);
        urlString = normalize(urlString, host, url);
        ans.add(new SimpleEntry<String, String>(urlString, aparse));
      }
    }
    return ans;
  }

  public static String normalize(String data, String host, String url) {
    if (data.isEmpty())
      return null;
    if (data.subSequence(0, 1).equals("h")) {
      data = data.replaceAll("(?is)^.*?(https?://)", "");
      return data;
    }
    if (data.subSequence(0, 1).equals("/")) {
      if (host.subSequence(host.length() - 1, host.length()).equals("/")) {
        host = host.substring(0, host.length() - 1);
      }
      data = host.concat(data);
      return data;
    }
    if (!url.subSequence(host.length() - 1, host.length()).equals("/")) {
      url = url.concat("/");
    }
    data = url.concat(data);
    return data;
  }

  public static void main(String[] args) {
    Options options = new Options();
    options.addOption(
        Option.builder().longOpt("help").desc("Print help message.").build());
    options.addOption(Option.builder().longOpt("file").argName("file").hasArg()
        .desc("Data path.").build());
    CommandLine line = CommandlineUtil.parse(options, args);

    LogUtil.check(logger, line.hasOption("file"), "Missing --file.");

    try {
      // String data = new String(Files.readAllBytes(Paths.get(line
      // .getOptionValue("file"))));
      String data =
          "<html><img>xxtt</img><img alt='haha'></img>gg<img alt=\"haha\" dsf>xxx</img>moma</html>";
      System.out.println(HtmlUtil.parseContent(data));
    } catch (Exception e) {
      LogUtil.error(logger, e);
    }
  }
}
