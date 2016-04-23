package sewm.bdbox.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

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

public class InfomallIndexer implements AutoCloseable {
  private static Logger logger = LogUtil.getLogger(InfomallIndexer.class);

  private static final String INFOMALL_COLLECTION_PREFIX = "Web_RAW_U";

  private Directory dir = null;
  private IndexWriter writer = null;
  private Set<String> ignoredCollections = null;

  private InfomallIndexer(Builder builder) throws IOException {
    Analyzer analyzer = new CJKAnalyzer();
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
    iwc.setRAMBufferSizeMB(builder.bufferSizeMB);
    dir = FSDirectory.open(Paths.get(builder.indexPath));
    writer = new IndexWriter(dir, iwc);
    ignoredCollections = builder.ignoredCollections;
  }

  public boolean index(String dataPath, String indexPath) {
    return indexDocCollections(Paths.get(dataPath));
  }

  private boolean indexDocCollections(Path path) {
    if (Files.isDirectory(path)) {
      try {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (file.startsWith(INFOMALL_COLLECTION_PREFIX)
                && ignoredCollections.contains(file.getFileName())) {
              indexDocCollection(file);
            } else {
              logger.info("Ignored " + file);
            }
            return FileVisitResult.CONTINUE;
          }
        });
        return true;
      } catch (Exception e) {
        LogUtil.error(logger, e);
        return false;
      }
    } else {
      return indexDocCollection(path);
    }
  }

  private boolean indexDocCollection(Path file) {
    try (SeekableInputStream is = new SeekableFileInputStream(new File(
        file.toString()))) {
      InfomallDocumentIterator iter = new InfomallDocumentIterator(is,
          file.toString());
      InfomallDocument doc;
      while ((doc = iter.next()) != null) {
        indexDoc(doc);
      }
      return true;
    } catch (Exception e) {
      LogUtil.error(logger, e);
      return false;
    }
  }

  private boolean indexDoc(InfomallDocument doc) {
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

  @Override
  public void close() throws IOException {
    if (writer != null) {
      writer.close();
    }
    if (dir != null) {
      dir.close();
    }
  }

  public static class Builder {
    private String indexPath;
    private double bufferSizeMB;
    private Set<String> ignoredCollections = new HashSet<String>();

    public Builder indexPath(String indexPath) {
      this.indexPath = indexPath;
      return this;
    }

    public Builder bufferSizeMB(double bufferSizeMB) {
      this.bufferSizeMB = bufferSizeMB;
      return this;
    }

    public Builder ignoreCollections(Set<String> ignoredCollections) {
      this.ignoredCollections = ignoredCollections;
      return this;
    }

    public Builder ignoreCollections(String ignoredCollectionsFile) {
      try {
        this.ignoredCollections = new HashSet<String>(Files.readAllLines(Paths
            .get(ignoredCollectionsFile)));
      } catch (IOException e) {
        logger.warn(ignoredCollectionsFile
            + "not found. Will not ignore any collection.");
      }
      return this;
    }

    public InfomallIndexer build() throws IOException {
      return new InfomallIndexer(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static void main(String[] args) {
    Options options = new Options();
    options.addOption(Option.builder().longOpt("help")
        .desc("Print help message.").build());
    options.addOption(Option.builder().longOpt("data").argName("path").hasArg()
        .desc("Data path.").build());
    options.addOption(Option.builder().longOpt("index").argName("path")
        .hasArg().desc("Index path.").build());
    options.addOption(Option.builder().longOpt("ignored_collections")
        .argName("path").hasArg().desc("Ignored collections path.").build());
    CommandLine line = CommandlineUtil.parse(options, args);

    LogUtil.check(logger, line.hasOption("data"), "Missing --data.");
    LogUtil.check(logger, line.hasOption("index"), "Missing --index.");
    InfomallIndexer.Builder builder = InfomallIndexer
        .builder()
        .indexPath(line.getOptionValue("data"))
        .ignoreCollections(
            line.getOptionValue("ignored_collections",
                "ignored_collections.txt"));

    try (InfomallIndexer indexer = builder.build()) {

    } catch (IOException e) {
      LogUtil.error(logger, e);
    }
  }
}