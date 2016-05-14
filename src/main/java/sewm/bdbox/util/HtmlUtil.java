package sewm.bdbox.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
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
    Pattern pattern = Pattern.compile("<title\\s*>(?<title>.*?)</title>",
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

  private static Pattern IMG_PATTERN = Pattern.compile(
      "<img\\s.*?(alt=(?<text>'.*?'|\".*?\"))?.*?>",
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
      if (replacement != null) {
        replacement = " " + replacement.substring(1, replacement.length() - 1)
            + " ";
        imgMatcher.appendReplacement(sb, replacement);
      }
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

  private static Pattern A_PATTERN = Pattern.compile(
      "<a(?<attr>.*?)>(?<atext>.*?)</a>",
      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  private static Pattern HREF_PATTERN = Pattern.compile(
      "href=(?<url>'.*?'|\".*?\")", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

  public static List<Entry<String, String>> parseAnchors(String data,
      String host, String url) {
    List<Entry<String, String>> anchors = new ArrayList<Entry<String, String>>();
    host = host.replaceAll("(?is)^https?://", "");
    url = url.replaceAll("(?is)^https?://", "");
    Matcher matcher = A_PATTERN.matcher(data);

    while (matcher.find()) {
      String atext = matcher.group("atext");
      atext = parseContent(atext);
      atext = atext.trim();
      if (atext.isEmpty()) {
        continue;
      }
      Matcher matcher2 = HREF_PATTERN.matcher(matcher.group("attr"));
      if (matcher2.find()) {
        String urlString = matcher2.group("url");
        urlString = extract(urlString, host, url);
        if (urlString == null || urlString.isEmpty())
          continue;
        anchors.add(new SimpleEntry<String, String>(urlString, atext));
      }
    }
    return anchors;
  }

  private static String extract(String data, String host, String url) {
    data = data.substring(1, data.length() - 1);

    if (data.startsWith("javascript")) {
      return null;
    } else if (data.startsWith("/")) {
      data = host.concat(data);
    } else if (!url.endsWith("/")) {
      data = url.concat(data);
    }
    data = normalize(data);
    return data;
  }

  private static String simplifyUrlPath(String data) {
    String[] items = data.split("/");
    Stack<String> stack = new Stack<String>();

    for (String item : items) {
      if (item.isEmpty() || item.equals(".")) {
        continue;
      } else if (item.equals("..")) {
        // The first item is host name instead of a part of path.
        if (stack.size() > 1) {
          stack.pop();
        }
      } else {
        stack.push(item);
      }
    }
    return String.join("/", stack);
  }

  static String normalize(String data) {
    data = data.replaceAll("(?is)^.*https?://", "");
    data = simplifyUrlPath(data);
    data = data.replaceAll("(?is)^www.", "");
    data = data.replaceAll("\\p{Punct}", " ");
    data = data.replaceAll("\\s+", " ");
    data = data.trim();
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
      String data = "202.102.148.186/?oldNews/2001/12x/2420.htm/../../..//yiliao-b,a&o-%j#i'a\"n/baojian.htm";
      System.out.println(HtmlUtil.normalize(data));
    } catch (Exception e) {
      LogUtil.error(logger, e);
    }
  }
}
