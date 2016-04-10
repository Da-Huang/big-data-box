package sewm.bdbox.search;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import sewm.bdbox.util.ExceptionUtil;
import sewm.bdbox.util.Initializer;

public class InformallIndexer {
  private static Logger logger = LogManager.getLogger(InformallIndexer.class);

  public static boolean index(String dataPath, String indexPath) {
    try (Directory dir = FSDirectory.open(Paths.get(indexPath))) {
      Analyzer analyzer = new CJKAnalyzer();
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
      iwc.setOpenMode(OpenMode.CREATE);
      IndexWriter writer = new IndexWriter(dir, iwc);
      indexDocCollections(writer, Paths.get(dataPath));
      return true;
    } catch (Exception e) {
      logger.error(ExceptionUtil.getStacktraceString(e));
    }
    return true;
  }

  private static boolean indexDocCollections(IndexWriter writer, Path path) {
    if (Files.isDirectory(path)) {
      try {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            indexDocCollection(writer, file);
            return FileVisitResult.CONTINUE;
          }
        });
        return true;
      } catch (Exception e) {
        logger.error(ExceptionUtil.getStacktraceString(e));
        return false;
      }
    } else {
      return indexDocCollection(writer, path);
    }
  }

  private static boolean indexDocCollection(IndexWriter writer, Path file) {
    try (InputStream stream = Files.newInputStream(file)) {
      // TODO: Split documents.
      indexDoc(writer, "");
      return true;
    } catch (Exception e) {
      logger.error(ExceptionUtil.getStacktraceString(e));
      return false;
    }
  }

  private static boolean indexDoc(IndexWriter writer, String... content) {
    try {
      Document doc = new Document();
      // TODO: Build index for items.
      // Field pathField = new StringField("path", file.toString(),
      // Field.Store.YES);
      // doc.add(pathField);
      //
      // doc.add(new TextField("contents", new BufferedReader(
      // new InputStreamReader(stream, StandardCharsets.UTF_8))));
      writer.addDocument(doc);
      return true;
    } catch (IOException e) {
      logger.error(ExceptionUtil.getStacktraceString(e));
      return false;
    }
  }

  public static void main(String[] args) {
    Initializer.init(InformallIndexer.class.getName(), args);
    InformallIndexer.index("test", "");
  }
}