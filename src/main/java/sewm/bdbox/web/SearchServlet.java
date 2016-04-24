package sewm.bdbox.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

import sewm.bdbox.util.InfomallSearchUtil;
import sewm.bdbox.util.InfomallWebQueryUtil;
import sewm.bdbox.util.LogUtil;

public class SearchServlet extends HttpServlet {
  private static final Logger logger = LogUtil.getLogger(SearchServlet.class);
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
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
        "start=%s, limit=%s, url=%s, host=%s, start_date=%s, end_date=%s, "
            + "text=%s, title=%s, content=%s, titleBoost=%s",
        start, limit, url, host, startDate, endDate, text, title, content,
        titleBoost));

    Query query = InfomallWebQueryUtil.parseQuery(url, host, startDate, endDate,
        text, title, content, titleBoost);
    logger.info("Query: " + query);

    if (query == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid query.");
      return;
    }

    resp.setContentType("application/json");
    resp.setCharacterEncoding("utf8");
    int startInt = InfomallWebQueryUtil.parseStart(start);
    int limitInt = InfomallWebQueryUtil.parseLimit(limit);
    TopDocs top = WebSingleton.getInfomallSearcher().search(query,
        startInt + limitInt);
    InfomallSearchUtil.writeResultAsJson(resp.getWriter(),
        ContentServlet.DATA_ROOT_PATH, WebSingleton.getInfomallSearcher(), top,
        startInt);
  }

  public static void main(String[] args) {
  }
}
