package sewm.bdbox.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.Query;

import sewm.bdbox.util.InfomallQueryUtil;
import sewm.bdbox.util.LogUtil;

public class SearchServlet extends HttpServlet {
  private static final Logger logger = LogUtil.getLogger(SearchServlet.class);
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/html");

    String start = req.getParameter("start");
    String limit = req.getParameter("limit");

    String url = req.getParameter("url");
    String host = req.getParameter("host");
    String startDate = req.getParameter("start_date");
    String endDate = req.getParameter("end_date");

    String text = req.getParameter("text");
    String title = req.getParameter("title");
    String content = req.getParameter("content");
    String titleBoost = req.getParameter("title_boost");

    logger.info(String.format(
        "start=%s, limit=%s, url=%s, host=%s, start_date=%s, end_date=%s, text=%s, title=%s, content=%s, titleBoost=%s",
        url, host, startDate, endDate, text, title, content, titleBoost));

    Query query = InfomallQueryUtil.parseQuery(url, host, startDate, endDate,
        text, title, content, titleBoost);
    logger.info(query);
  }

  public static void main(String[] args) {
    System.out.println(Long.parseLong(null, 3));
  }
}
