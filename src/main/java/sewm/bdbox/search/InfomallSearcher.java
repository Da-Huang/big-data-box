package sewm.bdbox.search;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import sewm.bdbox.util.LogUtil;

public class InfomallSearcher implements AutoCloseable {
  private static Logger logger = LogUtil.getLogger(InfomallSearcher.class);

  private IndexReader reader;
  private IndexSearcher searcher;

  public InfomallSearcher(String indexPath) {
    try {
      reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
      searcher = new IndexSearcher(reader);
    } catch (Exception e) {
      LogUtil.error(logger, e);
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e1) {
          LogUtil.error(logger, e1);
        }
      }
    }
  }

  public TopDocs search(Query query, int n) {
    try {
      return searcher.search(query, n);
    } catch (IOException e) {
      LogUtil.error(logger, e);
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
    // TODO Jargs命令行参数
    InfomallIndexer.index("F:/U200201/Web_Raw.U200201.0001", "F:/file");
  }
}