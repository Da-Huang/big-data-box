package sewm.bdbox.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.tukaani.xz.SeekableFileInputStream;
import org.tukaani.xz.SeekableInputStream;

import sewm.bdbox.util.CommandlineUtil;
import sewm.bdbox.util.LogUtil;

public class InfomallIndexer {
  private static Logger logger = LogUtil.getLogger(InfomallIndexer.class);

  public static boolean index(String dataPath, String indexPath) {
    try (Directory dir = FSDirectory.open(Paths.get(indexPath))) {
      Analyzer analyzer = new CJKAnalyzer(); // chinese
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
      iwc.setOpenMode(OpenMode.CREATE);
      IndexWriter writer = new IndexWriter(dir, iwc);
      indexDocCollections(writer, Paths.get(dataPath));
      return true;
    } catch (Exception e) {
      LogUtil.error(logger, e);
    }
    return true;
  }

  private static boolean indexDocCollections(IndexWriter writer, Path path) {
    if (Files.isDirectory(path)) {
      try {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() { // 递归文件目录，每遇到新文件callsimple功能
              @Override
              public FileVisitResult visitFile(Path file,
                  BasicFileAttributes attrs) {// only path
                // TODO:处理判断
                indexDocCollection(writer, file); // 加过滤，有非数据文件，通过后缀处理

                return FileVisitResult.CONTINUE;
              }
            });
        return true;
      } catch (Exception e) {
        LogUtil.error(logger, e);
        return false;
      }
    } else {
      return indexDocCollection(writer, path);
    }
  }

  private static boolean indexDocCollection(IndexWriter writer, Path file) {
    try (SeekableInputStream is = new SeekableFileInputStream(new File(
        file.toString()))) {
      InfomallDocumentIterator iter = new InfomallDocumentIterator(is,
          file.toString());
      InfomallDocument doc;
      int i = 0;
      while ((doc = iter.next()) != null) {
        logger.info(++i);
        indexDoc(writer, doc);
      }

      return true;
    } catch (Exception e) {
      LogUtil.error(logger, e);
      return false;
    }
  }

  private static boolean indexDoc(IndexWriter writer, InfomallDocument doc) {
    try {
      Document doc1 = new Document();
      doc1.add(new StoredField("path", doc.getPath()));
      doc1.add(new StoredField("position", doc.getPosition()));
      doc1.add(new TextField("title", doc.getTitle(), Field.Store.NO));
      doc1.add(new TextField("content", doc.getContent(), Field.Store.NO));
      doc1.add(new StringField("url", doc.getUrl(), Field.Store.NO));
      doc1.add(new StringField("version", doc.getVersion(), Field.Store.NO));
      doc1.add(new StringField("host", doc.getHost(), Field.Store.NO));
      writer.addDocument(doc1);
      return true;
    } catch (IOException e) {
      LogUtil.error(logger, e);
      return false;
    }
  }

  public static void main(String[] args) {
    Options options = new Options();
    options.addOption(Option.builder().longOpt("help")
        .desc("Print help message.").build());
    options.addOption(Option.builder().longOpt("data").argName("path").hasArg()
        .desc("Data path.").build());
    options.addOption(Option.builder().longOpt("index").argName("path")
        .hasArg().desc("Index path.").build());
    CommandLine line = CommandlineUtil.parse(options, args);
    LogUtil.check(logger, line.hasOption("data"), "Missing --data.");
    LogUtil.check(logger, line.hasOption("index"), "Missing --index.");
  }
}