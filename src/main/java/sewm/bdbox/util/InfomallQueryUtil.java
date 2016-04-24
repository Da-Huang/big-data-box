package sewm.bdbox.util;

import org.apache.logging.log4j.Logger;
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

public class InfomallQueryUtil {
  private static final Logger logger = LogUtil
      .getLogger(InfomallQueryUtil.class);

  public static Query parseTitleQuery(String title, float boost) {
    if (title == null) {
      return null;
    }
    QueryParser parser = new QueryParser("title",
        new SmartChineseAnalyzer(true));
    try {
      return new BoostQuery(parser.parse(title), boost);
    } catch (ParseException e) {
      LogUtil.error(logger, e);
      return null;
    }
  }

  public static Query parseContentQuery(String content) {
    if (content == null) {
      return null;
    }
    QueryParser parser = new QueryParser("content",
        new SmartChineseAnalyzer(true));
    try {
      return parser.parse(content);
    } catch (ParseException e) {
      LogUtil.error(logger, e);
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
    Query textContentQuery = parseContentQuery(content);
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
        LogUtil.error(logger, e);
      }
    }
    if (endDate != null) {
      try {
        end = Long.parseLong(endDate);
      } catch (NumberFormatException e) {
        LogUtil.error(logger, e);
      }
    }
    return start == Long.MIN_VALUE && end == Long.MAX_VALUE
        ? LongPoint.newRangeQuery("date", start, end) : null;
  }

  public static Query parseUrlQuery(String url) {
    return url == null ? null : new TermQuery(new Term("url", url));
  }

  public static Query parseHostQuery(String host) {
    return host == null ? null : new TermQuery(new Term("host", host));
  }

  public static float parseTitleBoost(String titleBoost) {
    try {
      return Float.parseFloat(titleBoost);
    } catch (NumberFormatException e) {
      LogUtil.error(logger, e);
      return 10f;
    }
  }
}
