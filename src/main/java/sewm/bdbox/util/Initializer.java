package sewm.bdbox.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

public class Initializer {
  public static void init(String name, String[] args) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    System.setProperty("current.date", dateFormat.format(new Date()));
    System.setProperty("log.filename",
        name + "." + System.getProperty("current.date"));
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    ctx.reconfigure();
  }
}
