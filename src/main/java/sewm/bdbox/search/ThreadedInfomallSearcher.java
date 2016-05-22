package sewm.bdbox.search;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.IndexSearcher;

import sewm.bdbox.util.LogUtil;

public class ThreadedInfomallSearcher extends InfomallSearcher {
  private static final Logger logger = LogUtil
      .getLogger(ThreadedInfomallSearcher.class);

  private ExecutorService executor = null;

  public ThreadedInfomallSearcher(int threads, String... indexPaths)
      throws IOException {
    logger.info("Using " + threads + " threads at most to build index.");
    executor = Executors.newFixedThreadPool(threads);
    reader = openIndexReader(indexPaths);
    searcher = new IndexSearcher(reader, executor);
  }

  @Override
  public void close() throws IOException {
    super.close();
    executor.shutdown();
    try {
      executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LogUtil.error(logger, e);
    }
  }
}
