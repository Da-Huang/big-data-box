package sewm.bdbox.util;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class LogManager {
  static {
    final LoggerContext ctx = (LoggerContext) org.apache.logging.log4j.LogManager
        .getContext(false);
    final Configuration config = ctx.getConfiguration();
    Layout<?> layout = PatternLayout.createLayout(
        "%p: %d{yyyy-MM-dd HH:mm:ss} [%t] %C:%M(%L): %msg%xEx%n" /* pattern */,
        null /* patternSelector */, config, null /* replace */,
        StandardCharsets.UTF_8, true /* alwaysWriteExceptions */,
        false /* noConsoleNoAnsi */, null /* header */, null /* footer */);

    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-hhmmss");
    String filename = String.format("logs/%s.%s.log", formatter
        .format(new Date()),
        System.getProperty("sun.java.command").split("\\s+", 2)[0]);

    Appender appender = FileAppender.createAppender(filename /* fileName */,
        "false" /* append */, null /* locking */, "File" /* name */,
        null /* immediateFlush */, null /* ignore */, null /* bufferIo */,
        null /* bufferSizeStr */, layout, null /* filter */,
        "false" /* advertise */, null /* advertiseUri */, config);
    appender.start();
    config.addAppender(appender);
    for (final LoggerConfig loggerConfig : config.getLoggers().values()) {
      loggerConfig.addAppender(appender, null, null);
    }
    config.getRootLogger().addAppender(appender, null, null);
  }

  public static Logger getLogger(String name) {
    return org.apache.logging.log4j.LogManager.getLogger(name);
  }

  public static Logger getLogger(Class<?> clazz) {
    return org.apache.logging.log4j.LogManager.getLogger(clazz);
  }
}
