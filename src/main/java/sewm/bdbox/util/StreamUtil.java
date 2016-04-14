package sewm.bdbox.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StreamUtil {
  private static Logger logger = LogManager.getLogger(StreamUtil.class);
  
  public static String readLine(InputStream is) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      int i = is.read();
      if (i == -1) {
        return null;
      }
      while (i != -1 && i != '\n') {
        outputStream.write(i);
        i = is.read();
      }
      return new String(outputStream.toByteArray());
    } catch (IOException e) {
      logger.error(ExceptionUtil.getStacktraceString(e));
      return null;
    }
  }
  
  public static void main(String[] args) {
    try (InputStream is = Files.newInputStream(Paths.get("pom.xml"))) {
      String line;
      while ((line = readLine(is)) != null) {
        System.out.println(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}