package sewm.bdbox.search;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import sewm.bdbox.util.CommandlineUtil;
import sewm.bdbox.util.LogUtil;

public class InfomallSearcher implements AutoCloseable {
  private static Logger logger = LogUtil.getLogger(InfomallSearcher.class);

  private IndexReader reader = null;
  private IndexSearcher searcher = null;

  public InfomallSearcher(String indexPath) throws IOException {
    reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
    searcher = new IndexSearcher(reader);
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
  public void close() throws IOException {
    if (reader != null) {
      reader.close();
    }
  }

  public static void main(String[] args) {
    Options options = new Options();
    options.addOption(Option.builder().longOpt("help")
        .desc("Print help message.").build());
    options.addOption(Option.builder().longOpt("index").argName("path")
        .hasArg().desc("Index path.").build());
    CommandLine line = CommandlineUtil.parse(options, args);
    LogUtil.check(logger, line.hasOption("index"), "Missing --index.");
  }
}