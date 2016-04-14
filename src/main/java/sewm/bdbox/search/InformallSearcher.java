package sewm.bdbox.search;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import sewm.bdbox.util.ExceptionUtil;

public class InformallSearcher implements AutoCloseable {
  private static Logger logger = LogManager.getLogger(InformallSearcher.class);

  private IndexReader reader;
  private IndexSearcher searcher;

  public InformallSearcher(String indexPath) {
    try {
      reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
      searcher = new IndexSearcher(reader);
    } catch (Exception e) {
      logger.error(ExceptionUtil.getStacktraceString(e));
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e1) {
          logger.error(ExceptionUtil.getStacktraceString(e1));
        }
      }
    }
  }

  public TopDocs search(Query query, int n) {
    try {
      return searcher.search(query, n);
    } catch (IOException e) {
      logger.error(ExceptionUtil.getStacktraceString(e));
      return null;
    }
  }

  @Override
  public void close() throws Exception {
    if (reader != null) {
      reader.close();
    }
  }

  public static void main(String[] args) {
  }
}