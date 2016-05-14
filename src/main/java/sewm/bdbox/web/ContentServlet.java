package sewm.bdbox.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;

import sewm.bdbox.search.InfomallDocument;
import sewm.bdbox.util.InfomallFetchUtil;
import sewm.bdbox.util.LogUtil;

public class ContentServlet extends HttpServlet {
  private static final Logger logger = LogUtil.getLogger(ContentServlet.class);
  private static final long serialVersionUID = 1L;

  static final String DATA_ROOT_PATH = WebSingleton.getProperties()
      .getProperty("data");

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/html");
    final String CONTENT = "/content/";
    logger.info(req.getRequestURL());
    int idIndex = req.getRequestURI().indexOf(CONTENT);
    if (idIndex < 0) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id not found.");
      return;
    }
    String idStr = req.getRequestURI().substring(idIndex + CONTENT.length());
    try {
      int id = Integer.parseInt(idStr);
      Document doc = WebSingleton.getInfomallSearcher().doc(id);
      InfomallDocument infomallDoc = InfomallFetchUtil.fetch(DATA_ROOT_PATH,
          doc.get("filename"), Long.parseLong(doc.get("position")));
      resp.getOutputStream().write(infomallDoc.getUnzipBytes());
    } catch (NumberFormatException e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          idStr + " is not a valid id.");
    }
  }
}
