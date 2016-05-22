package sewm.bdbox.util;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class InfomallWebQueryUtil {
  private static final Logger logger = LogUtil
      .getLogger(InfomallWebQueryUtil.class);

  private static final Analyzer ANALYZER = new SmartChineseAnalyzer(true);
  private static final QueryParser TITLE_QUERY_PARSER = new QueryParser("title",
      ANALYZER);

  public static Query parseTitleQuery(String title, float boost) {
    if (title == null) {
      return null;
    }
    try {
      return new BoostQuery(TITLE_QUERY_PARSER.parse(title), boost);
    } catch (ParseException e) {
      logger.error(e);
      return null;
    }
  }

  private static final QueryParser CONTENT_QUERY_PARSER = new QueryParser(
      "content", ANALYZER);

  public static Query parseContentQuery(String content) {
    if (content == null) {
      return null;
    }
    try {
      return CONTENT_QUERY_PARSER.parse(content);
    } catch (ParseException e) {
      logger.error(e);
      return null;
    }
  }

  public static Query parseQuery(String url, String host, String startDate,
      String endDate, String text, String title, String content,
      String titleBoost) {
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    // FILTER Queries.
    Query urlQuery = parseUrlQuery(url);
    if (urlQuery != null) {
      builder.add(urlQuery, Occur.FILTER);
    }
    Query hostQuery = parseHostQuery(host);
    if (hostQuery != null) {
      builder.add(hostQuery, Occur.FILTER);
    }
    Query dateQuery = parseDateQuery(startDate, endDate);
    if (dateQuery != null) {
      builder.add(dateQuery, Occur.FILTER);
    }

    // SHOULD Queries.
    float titleBoostFloat = parseTitleBoost(titleBoost);
    Query textTitleQuery = parseTitleQuery(text, titleBoostFloat);
    if (textTitleQuery != null) {
      builder.add(textTitleQuery, Occur.SHOULD);
    }
    Query textContentQuery = parseContentQuery(text);
    if (textContentQuery != null) {
      builder.add(textContentQuery, Occur.SHOULD);
    }

    // MUST Queries.
    Query titleQuery = parseTitleQuery(title, titleBoostFloat);
    if (titleQuery != null) {
      builder.add(titleQuery, Occur.MUST);
    }
    Query contentQuery = parseContentQuery(content);
    if (contentQuery != null) {
      builder.add(contentQuery, Occur.MUST);
    }

    BooleanQuery query = builder.build();
    return query.clauses().isEmpty() ? null : query;
  }

  public static Query parseDateQuery(String startDate, String endDate) {
    long start = Long.MIN_VALUE, end = Long.MAX_VALUE;
    if (startDate != null) {
      try {
        start = Long.parseLong(startDate);
      } catch (NumberFormatException e) {
        logger.error(e);
      }
    }
    if (endDate != null) {
      try {
        end = Long.parseLong(endDate);
      } catch (NumberFormatException e) {
        logger.error(e);
      }
    }
    return start == Long.MIN_VALUE && end == Long.MAX_VALUE ? null
        : LongPoint.newRangeQuery("date", start, end);
  }

  public static Query parseUrlQuery(String url) {
    return url == null ? null
        : new TermQuery(new Term("url", HtmlUtil.normalizeURL(url)));
  }

  public static Query parseHostQuery(String host) {
    return host == null ? null : new TermQuery(new Term("host", host));
  }

  public static float parseTitleBoost(String titleBoost) {
    if (titleBoost == null) {
      return 10f;
    }
    try {
      return Float.parseFloat(titleBoost);
    } catch (NumberFormatException e) {
      logger.error(e);
      return 10f;
    }
  }

  public static int parseStart(String start) {
    if (start == null) {
      return 0;
    }
    try {
      return Integer.parseInt(start);
    } catch (NumberFormatException e) {
      logger.error(e);
      return 0;
    }
  }

  public static int parseLimit(String limit) {
    if (limit == null) {
      return 10;
    }
    try {
      return Integer.parseInt(limit);
    } catch (NumberFormatException e) {
      logger.error(e);
      return 10;
    }
  }
}
