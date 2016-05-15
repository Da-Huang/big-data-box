package sewm.bdbox.search;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.Logger;

import sewm.bdbox.util.CommandlineUtil;
import sewm.bdbox.util.LogUtil;

public class InfomallDataMapper {
  private static final Logger logger = LogUtil
      .getLogger(InfomallSearcher.class);

  private static final Pattern INFOMALL_DATA_PATTERN = Pattern
      .compile("[A-Z]\\d{6}");

  public static Map<String, String> map(Path path) {
    Map<String, String> map = new HashMap<>();
    try {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir,
            BasicFileAttributes attrs) throws IOException {
          if (INFOMALL_DATA_PATTERN.matcher(dir.getFileName().toString())
              .matches()) {
            map.put(dir.getFileName().toString(), dir.getParent().toString());
            return FileVisitResult.SKIP_SUBTREE;
          } else {
            return FileVisitResult.CONTINUE;
          }
        }
      });
    } catch (IOException e) {
      LogUtil.error(logger, e);
    }
    return map;
  }

  public static void main(String[] args) {
    Options options = new Options();
    options.addOption(
        Option.builder().longOpt("help").desc("Print help message.").build());
    options.addOption(Option.builder().longOpt("data").argName("path").hasArg()
        .desc("Data root path.").build());
    options.addOption(Option.builder().longOpt("map").argName("path").hasArg()
        .desc("Data mapping output path.").build());
    CommandLine line = CommandlineUtil.parse(options, args);

    LogUtil.check(logger, line.hasOption("data"), "Missing --data.");
    LogUtil.check(logger, line.hasOption("map"), "Missing --map.");

    Map<String, String> dataMap = map(Paths.get(line.getOptionValue("data")));
    try {
      Files.write(Paths.get(line.getOptionValue("map")),
          dataMap.entrySet().stream().map(e -> e.getKey() + "\t" + e.getValue())
              .collect(Collectors.toList()));
    } catch (IOException e) {
      LogUtil.error(logger, e);
    }
  }
}
