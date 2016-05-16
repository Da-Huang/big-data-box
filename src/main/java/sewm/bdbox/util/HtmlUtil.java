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

  private static final Pattern TITLE_PATTERN = Pattern.compile(
      "<title\\s*>(?<title>.*?)</title>",
      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

  public static String extractTitle(String html) {
    Matcher matcher = TITLE_PATTERN.matcher(html);
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
        sb.append(" ");
      }
      sb.append(matcher.group("title"));
    }
    return sb.toString();
  }

  /**
   * @param cleanHtml
   *          is returned from extractCleanHtml.
   */
  public static String extractContentFromCleanHtml(String cleanHtml) {
    cleanHtml = cleanHtml.replaceAll("(?s)<\\w+?.*?>", " ");
    cleanHtml = cleanHtml.replaceAll("</\\w+?>", " ");
    cleanHtml = cleanHtml.replaceAll("\\s+", " ");
    return cleanHtml;
  }

  private static Pattern IMG_PATTERN = Pattern.compile(
      "<img\\s.*?(alt=(?<alt>'.*?'|\".*?\"))?.*?>",
      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

  public static String extractCleanHtml(String html) {
    html = html.replaceAll("\\s+>", ">");
    html = html.replaceAll("(?is)<head.*?>.*?</head>", " ");
    html = html.replaceAll("(?is)<script.*?>.*?</script>", " ");
    html = html.replaceAll("(?is)<style.*?>.*?</style>", " ");
    html = html.replaceAll("(?is)^.*?<html", "<html");
    html = html.replaceAll("(?s)<!--.*?-->", " ");
    html = html.replaceAll("(?s)<!.*?>", " ");
    // Removes begin tags except for <a> and <img>.
    html = html.replaceAll("(?is)<[b-hj-z].*?>", " ");

    // Extracts img alt.
    Matcher imgMatcher = IMG_PATTERN.matcher(html);
    StringBuffer sb = new StringBuffer();
    while (imgMatcher.find()) {
      String alt = imgMatcher.group("alt");
      if (alt != null) {
        alt = " " + alt.substring(1, alt.length() - 1) + " ";
        alt = alt.replaceAll("\\\\", "\\\\\\\\");
        alt = alt.replaceAll("\\$", "\\\\\\$");
        imgMatcher.appendReplacement(sb, alt);
      }
    }
    imgMatcher.appendTail(sb);
    html = sb.toString();

    // Removes end tags except for </a>.
    html = html.replaceAll("(?i)</[b-z]\\w*?>", " ");
    // Removes tags with at least 2 characters.
    html = html.replaceAll("(?s)<\\w\\w.*?>", " ");
    html = html.replaceAll("</\\w\\w+?>", " ");
    return html;
  }

  /**
   * @return null, if invalid.
   */
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
      "<a\\s.*?(href=(?<href>'.*?'|\".*?\"))?.*?>(?<text>.*?)</a>",
      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

  /**
   * @param cleanHtml
   *          is returned from extractCleanHtml.
   */
  public static List<Entry<String, String>> extractAnchorsFromCleanHtml(
      String cleanHtml, String host, String url) {
    List<Entry<String, String>> anchors = new ArrayList<Entry<String, String>>();
    Matcher matcher = A_PATTERN.matcher(cleanHtml);
    while (matcher.find()) {
      String href = matcher.group("href");
      if (href == null) {
        continue;
      }
      String text = matcher.group("text");
      text = extractContentFromCleanHtml(text);
      text = text.trim();
      if (text.isEmpty()) {
        continue;
      }

      href = href.substring(1, href.length() - 1);
      href = verifyUrl(href, host, url);
      if (href == null || href.isEmpty()) {
        continue;
      }
      anchors.add(new SimpleEntry<String, String>(text, href));
    }
    return anchors;
  }

  private static String verifyUrl(String href, String host, String url) {
    if (href.isEmpty() || href.startsWith("javascript:")
        || href.startsWith("#")) {
      return null;
    } else if (href.startsWith("?")) {
      return url.concat(href);
    } else if (href.startsWith("https://") || href.startsWith("http://")) {
      return href;
    } else if (href.startsWith("/")) {
      return host.concat(href);
    } else {
      if (url.endsWith("/")) {
        return url.concat(href);
      } else {
        return url + "/" + href;
      }
    }
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

  public static String normalizeURL(String url) {
    url = url.replaceAll("(?is)^.*https?://", "");
    url = simplifyUrlPath(url);
    url = url.replaceAll("(?is)^www.", "");
    url = url.replaceAll("\\p{Punct}", " ");
    url = url.replaceAll("\\s+", " ");
    url = url.trim();
    return url;
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
      // String data =
      // "202.102.148.186/?oldNews/2001/12x/2420.htm/../../..//yiliao-b,a&o-%j#i'a\"n/baojian.htm";
      // System.out.println(HtmlUtil.normalizeURL(data));
      // System.out.println(verifyUrl("?fdsfs", "a.com", "a.com/ewrwe"));
      // System.out.println(verifyUrl("#fdsfs", "a.com", "a.com/ewrwe/"));
      // String html = "<A href=''>xxx</A>";
      // html = "<IMG href=''>xdfs</IMG>";
      // html = HtmlUtil.extractCleanHtml(html);
      // System.out.println(html);
      String html = "C:\\DOCUME~1\\Goes\\LOCALS~1\\Temp\\((A$F[7FV{D223S{Q03%$MQ.jpg";
      System.out.println(html);
      String regex = "\\\\";
      System.out.println(regex);
      System.out.println(html.replaceAll("\\\\", "\\\\\\\\"));
    } catch (Exception e) {
      LogUtil.error(logger, e);
    }
  }
}
