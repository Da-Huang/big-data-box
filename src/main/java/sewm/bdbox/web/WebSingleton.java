package sewm.bdbox.web;

import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

import sewm.bdbox.search.InfomallSearcher;
import sewm.bdbox.search.ThreadedInfomallSearcher;
import sewm.bdbox.util.LogUtil;

class WebSingleton {
  private static Logger logger = LogUtil.getLogger(WebSingleton.class);

  private static Properties properties = null;

  static Properties getProperties() {
    if (properties == null) {
      synchronized (Properties.class) {
        if (properties == null) {
          try {
            properties = new Properties();
            properties.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("config.properities"));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return properties;
  }

  private static InfomallSearcher infomallSearcher = null;

  static InfomallSearcher getInfomallSearcher() {
    int threads = Integer.parseInt(getProperties().getProperty("threads", "1"));

    if (infomallSearcher == null) {
      synchronized (InfomallSearcher.class) {
        if (infomallSearcher == null) {
          try {
            infomallSearcher = new ThreadedInfomallSearcher(threads,
                getProperties().getProperty("index").split(";"));
          } catch (IOException e) {
            LogUtil.error(logger, e);
          }
        }
      }
    }
    return infomallSearcher;
  }

  public static void main(String[] args) {
    Properties prop = new Properties();
    try {
      prop.load(Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("config.properities"));
      System.out.println(prop.getProperty("index"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
