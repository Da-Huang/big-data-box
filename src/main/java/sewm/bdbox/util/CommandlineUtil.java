package sewm.bdbox.util;

import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.util.CommandLineUtil;

public class CommandlineUtil {
  private static Logger logger = LogUtil.getLogger(CommandLineUtil.class);

  public static CommandLine parse(Options options, String[] args) {
    CommandLineParser parser = new DefaultParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args);
      if (line.hasOption("help")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
            System.getProperty("sun.java.command").split("\\s+", 2)[0],
            options);
        System.exit(0);
      }
    } catch (ParseException e) {
      LogUtil.fatal(logger, e.getMessage());
    }
    return line;
  }

  public static void confirm(String msg) {
    System.out.print(msg);
    System.out.print(" ");
    Scanner scanner = new Scanner(new CloseShieldInputStream(System.in));
    String line = scanner.nextLine();
    boolean ans = line.toLowerCase().equals("y");
    scanner.close();
    if (!ans) {
      System.exit(0);
    }
  }
}
