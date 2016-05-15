package sewm.bdbox.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;

import sewm.bdbox.util.InfomallGraphUtil;
import sewm.bdbox.util.LogUtil;

public class GraphServlet extends HttpServlet {
  private static final Logger logger = LogUtil.getLogger(GraphServlet.class);
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String url = req.getParameter("url");
    logger.info("url=" + url);
    if (url == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid url.");
      return;
    }

    Map<String, String> in = InfomallGraphUtil.fetchInUrls(
        ContentServlet.DATA_MAP, WebSingleton.getInfomallSearcher(), url);
    Map<String, String> out = InfomallGraphUtil.fetchOutUrls(
        ContentServlet.DATA_MAP, WebSingleton.getInfomallSearcher(), url);

    InfomallGraphUtil.writeResultAsJson(resp.getWriter(), url, in, out);
  }
}
