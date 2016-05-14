package sewm.bdbox.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
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
import sewm.bdbox.util.HtmlUtil;
import sewm.bdbox.util.LogUtil;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class InfomallIndexer implements AutoCloseable {
  private static final Logger logger = LogUtil.getLogger(InfomallIndexer.class);

  protected static final String INFOMALL_COLLECTION_PREFIX = "Web_Raw.";

  private Directory dir = null;
  protected IndexWriter writer = null;
  protected Set<String> ignoredCollections = null;

  private List<Runnable> onCloseActions = new ArrayList<Runnable>();
  private boolean stop = false;

  protected InfomallIndexer(Builder builder) throws IOException {
    Analyzer analyzer = new SmartChineseAnalyzer(true);
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    iwc.setOpenMode(
        builder.create ? OpenMode.CREATE : OpenMode.CREATE_OR_APPEND);
    iwc.setRAMBufferSizeMB(builder.bufferSizeMB);
    dir = FSDirectory.open(Paths.get(builder.indexPath));
    writer = new IndexWriter(dir, iwc);
    ignoredCollections = builder.ignoredCollections;
  }

  public boolean index(String dataPath) {
    return indexDocCollections(Paths.get(dataPath));
  }

  public boolean indexDocCollections(Path path) {
    if (Files.isDirectory(path)) {
      try {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file,
              BasicFileAttributes attrs) {
            processDocCollection(file);
            return FileVisitResult.CONTINUE;
          }
        });
        return true;
      } catch (Exception e) {
        LogUtil.error(logger, e);
        return false;
      }
    } else {
      return processDocCollection(path);
    }
  }

  protected boolean processDocCollection(Path file) {
    if (stop) {
      logger.info("Skipped " + file + ", as this writer has been stopped.");
      return false;
    }
    if (file.getFileName().toString().startsWith(INFOMALL_COLLECTION_PREFIX)
        && !ignoredCollections.contains(file.getFileName().toString())) {
      logger.info("Currently " + writer.numDocs()
          + " documents indexed. Go on processing " + file.getFileName());
      int numDocs = writer.numDocs();
      boolean success = indexDocCollection(file);
      if (success) {
        logger.info("Processed " + file.getFileName() + "["
            + (writer.numDocs() - numDocs) + "], and ignore it.");
        ignoredCollections.add(file.getFileName().toString());
      } else {
        logger.info("Failed to process " + file.getFileName() + ".");
      }
      return success;
    } else {
      logger.info("Ignored " + file);
      return false;
    }
  }

  public boolean indexDocCollection(Path file) {
    try (SeekableInputStream is = new SeekableFileInputStream(
        new File(file.toString()))) {
      InfomallDocumentIterator iter = new InfomallDocumentIterator(is,
          file.getFileName().toString());
      InfomallDocument doc;
      while ((doc = iter.next()) != null) {
        indexDoc(doc);
        if (writer.numDocs() % 10000 == 0) {
          logger.info("Indexed " + writer.numDocs() + " documents.");
        }
      }
      return true;
    } catch (Exception e) {
      LogUtil.error(logger, e);
      return false;
    }
  }

  public boolean indexDoc(InfomallDocument infomallDoc) {
    try {
      Document doc = new Document();
      doc.add(new StoredField("filename", infomallDoc.getFilename()));
      doc.add(new StoredField("position", infomallDoc.getPosition()));

      doc.add(new StringField("url",
          HtmlUtil.normalizeURL(infomallDoc.getUrl()), Field.Store.NO));
      doc.add(new StringField("host", infomallDoc.getHost(), Field.Store.NO));
      doc.add(new LongPoint("date", infomallDoc.getDate().getTime()));
      doc.add(new TextField("title", infomallDoc.getTitle(), Field.Store.NO));
      doc.add(
          new TextField("content", infomallDoc.getContent(), Field.Store.NO));
      if (infomallDoc.getOrigin() != null) {
        doc.add(
            new StringField("origin", infomallDoc.getOrigin(), Field.Store.NO));
      }
      if (infomallDoc.getAnchors() != null) {
        for (Entry<String, String> anchor : infomallDoc.getAnchors()) {
          doc.add(
              new StringField("anchor_url", anchor.getValue(), Field.Store.NO));
        }
      }
      writer.addDocument(doc);
      return true;
    } catch (IOException e) {
      LogUtil.error(logger, e);
      return false;
    }
  }

  /**
   * This function is not thread-safe.
   */
  public void onCloseWriteIgnoredCollections(String ignoredCollectionsFile) {
    onCloseWriteIgnoredCollections(Paths.get(ignoredCollectionsFile));
  }

  /**
   * This function is not thread-safe.
   */
  public void onCloseWriteIgnoredCollections(Path ignoredCollections) {
    onCloseActions.add(new Runnable() {
      @Override
      public void run() {
        logger.info("On-close Action: writing ignoredCollections("
            + InfomallIndexer.this.ignoredCollections.size() + ").");
        try {
          Files.write(ignoredCollections,
              InfomallIndexer.this.ignoredCollections);
        } catch (IOException e) {
          LogUtil.error(logger, e);
        }
      }
    });
  }

  public void stop() {
    stop = true;
    logger.info("Stoping this writer.");
  }

  @Override
  public void close() throws IOException {
    logger.info("Totally " + writer.numDocs() + " documents has been indexed.");
    logger.info("Doing on-close actions(" + onCloseActions.size() + ").");
    for (Runnable action : onCloseActions) {
      action.run();
    }
    logger.info("Closing this writer.");
    if (writer != null) {
      writer.close();
    }
    if (dir != null) {
      dir.close();
    }
    logger.info("Closed this writer.");
  }

  public static class Builder {
    private String indexPath;
    private double bufferSizeMB = 2048;
    private Set<String> ignoredCollections = new HashSet<String>();
    private boolean create = false;

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

    public Builder create() {
      this.create = true;
      return this;
    }

    public Builder create(boolean create) {
      this.create = create;
      return this;
    }

    public Builder ignoreCollections(String ignoredCollectionsFile) {
      try {
        this.ignoredCollections = new HashSet<String>(
            Files.readAllLines(Paths.get(ignoredCollectionsFile)));
        logger.info("Loaded " + this.ignoredCollections.size()
            + " ignored collections from " + ignoredCollectionsFile);
      } catch (IOException e) {
        logger.warn(ignoredCollectionsFile
            + " not found. Will not ignore any collection.");
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
    options.addOption(
        Option.builder().longOpt("help").desc("Print help message.").build());
    options.addOption(Option.builder().longOpt("data").argName("path").hasArg()
        .desc("Data path.").build());
    options.addOption(Option.builder().longOpt("index").argName("dir").hasArg()
        .desc("Index path.").build());
    options.addOption(Option.builder().longOpt("ignored_collections")
        .argName("path").hasArg().desc("Ignored collections path.").build());
    options.addOption(Option.builder().longOpt("create")
        .desc("Create new index. Otherwise, append data to the old index.")
        .build());
    options.addOption(Option.builder().longOpt("skip_confirmation")
        .desc("Skip all confirmation.").build());
    CommandLine line = CommandlineUtil.parse(options, args);

    LogUtil.check(logger, line.hasOption("data"), "Missing --data.");
    LogUtil.check(logger, line.hasOption("index"), "Missing --index.");
    if (!line.hasOption("skip_confirmation")) {
      if (Files.exists(Paths.get(line.getOptionValue("index")))) {
        CommandlineUtil.confirm(line.getOptionValue("index")
            + " already exists. Are you sure to build index on it? [y/n]");
      }
      if (line.hasOption("create")) {
        CommandlineUtil.confirm(
            "create mode will overwrite the old index. Are you sure? [y/n]");
      }
    }

    String ignoredCollectionsFile = line.getOptionValue("ignored_collections",
        "ignored_collections.txt");
    InfomallIndexer.Builder builder = InfomallIndexer.builder()
        .indexPath(line.getOptionValue("index"))
        .ignoreCollections(ignoredCollectionsFile)
        .create(line.hasOption("create"));

    try (InfomallIndexer indexer = builder.build()) {
      indexer.index(line.getOptionValue("data"));
      indexer.onCloseWriteIgnoredCollections(ignoredCollectionsFile);
      Signal.handle(new Signal("INT"), new SignalHandler() {
        public void handle(Signal sig) {
          indexer.stop();
        }
      });
    } catch (IOException e) {
      LogUtil.error(logger, e);
    }
  }
}
