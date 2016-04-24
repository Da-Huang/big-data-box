package sewm.bdbox.search;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import sewm.bdbox.util.CommandlineUtil;
import sewm.bdbox.util.InfomallDocumentFetchUtil;
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

  public Document doc(int docID) {
    try {
      return searcher.doc(docID);
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
    options.addOption(
        Option.builder().longOpt("help").desc("Print help message.").build());
    options.addOption(Option.builder().longOpt("data").argName("path").hasArg()
        .desc("Data root path.").build());
    options.addOption(Option.builder().longOpt("index").argName("dir").hasArg()
        .desc("Index path.").build());
    options.addOption(Option.builder().longOpt("query").argName("string")
        .hasArg().desc("Query string.").build());
    options.addOption(Option.builder().longOpt("limit").argName("int").hasArg()
        .desc("Limited number of displayed documents.").build());
    CommandLine line = CommandlineUtil.parse(options, args);

    LogUtil.check(logger, line.hasOption("index"), "Missing --index.");
    LogUtil.check(logger, line.hasOption("data"), "Missing --data.");
    LogUtil.check(logger, line.hasOption("query"), "Missing --query.");

    int limit = Integer.getInteger(line.getOptionValue("limit"), 10);
    Analyzer analyzer = new SmartChineseAnalyzer(true);
    QueryParser titleParser = new QueryParser("title", analyzer);
    QueryParser contentParser = new QueryParser("content", analyzer);
    try (InfomallSearcher searcher = new InfomallSearcher(
        line.getOptionValue("index"))) {
      BooleanQuery.Builder builder = new BooleanQuery.Builder();
      builder.add(
          new BoostQuery(titleParser.parse(line.getOptionValue("query")), 10),
          Occur.SHOULD);
      builder.add(contentParser.parse(line.getOptionValue("query")),
          Occur.SHOULD);
      Query query = builder.build();
      logger.info("Querying: " + query);
      TopDocs top = searcher.search(query, limit);
      logger.info("Hit " + top.totalHits + " documents.");
      for (ScoreDoc scoreDoc : top.scoreDocs) {
        Document doc = searcher.doc(scoreDoc.doc);
        logger.info(doc.get("filename") + ", " + doc.get("position"));
        InfomallDocument infomallDoc = InfomallDocumentFetchUtil.fetch(
            line.getOptionValue("data"), doc.get("filename"),
            doc.get("position"));
        logger.info(infomallDoc.getUrl() + " " + infomallDoc.getTitle());
      }
    } catch (Exception e) {
      LogUtil.error(logger, e);
    }
  }
}