package sewm.bdbox.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.Logger;

import sewm.bdbox.util.CommandlineUtil;
import sewm.bdbox.util.LogUtil;

public class ThreadedInfomallIndexer extends InfomallIndexer {
  private static final Logger logger = LogUtil
      .getLogger(ThreadedInfomallIndexer.class);

  private ExecutorService executor = null;

  private ThreadedInfomallIndexer(Builder builder) throws IOException {
    super(builder);
    executor = Executors.newFixedThreadPool(builder.threads);
  }

  @Override
  protected boolean processDocCollection(Path file) {
    if (file.getFileName().toString().startsWith(INFOMALL_COLLECTION_PREFIX)
        && !ignoredCollections.contains(file.getFileName().toString())) {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          ThreadedInfomallIndexer.super.processDocCollection(file);
        }
      });
      return true;
    } else {
      logger.info("Ignored " + file);
      return false;
    }
  }

  @Override
  public void close() throws IOException {
    executor.shutdown();
    try {
      executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LogUtil.error(logger, e);
    }
    super.close();
  }

  public static class Builder extends InfomallIndexer.Builder {
    private int threads;

    public Builder threads(int threads) {
      this.threads = threads;
      return this;
    }

    @Override
    public InfomallIndexer build() throws IOException {
      return new ThreadedInfomallIndexer(this);
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
    options.addOption(Option.builder().longOpt("index").argName("dir").hasArg()
        .desc("Index path.").build());
    options.addOption(Option.builder().longOpt("ignored_collections")
        .argName("path").hasArg().desc("Ignored collections path.").build());
    options.addOption(Option.builder().longOpt("threads").argName("int")
        .hasArg().desc("Number of threads.").build());
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
        CommandlineUtil
            .confirm("create mode will overwrite the old index. Are you sure? [y/n]");
      }
    }

    String ignoredCollectionsFile =
        line.getOptionValue("ignored_collections", "ignored_collections.txt");
    int threads = Integer.parseInt(line.getOptionValue("threads", "1"));
    InfomallIndexer.Builder builder =
        ThreadedInfomallIndexer.builder().threads(threads)
            .indexPath(line.getOptionValue("index"))
            .ignoreCollections(ignoredCollectionsFile)
            .create(line.hasOption("create"));

    try (InfomallIndexer indexer = builder.build()) {
      indexer.index(line.getOptionValue("data"));
      indexer.onCloseWriteIgnoredCollections(ignoredCollectionsFile);
    } catch (IOException e) {
      LogUtil.error(logger, e);
    }
  }
}
