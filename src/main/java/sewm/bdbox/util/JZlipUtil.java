package sewm.bdbox.util;

import java.io.ByteArrayOutputStream;
import java.util.zip.Inflater;

import org.apache.logging.log4j.Logger;

public class JZlipUtil {
  private static Logger logger = LogManager.getLogger(JZlipUtil.class);

  public static byte[] decompress(byte[] data) {
    Inflater inflater = new Inflater();
    inflater.setInput(data);
    byte[] buffer = new byte[1024];
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      while (!inflater.finished()) {
        int count = inflater.inflate(buffer);
        outputStream.write(buffer, 0, count);
      }
      byte[] output = outputStream.toByteArray();
      return output;
    } catch (Exception e) {
      logger.error(ExceptionUtil.getStacktraceString(e));
      return null;
    }
  }
}
