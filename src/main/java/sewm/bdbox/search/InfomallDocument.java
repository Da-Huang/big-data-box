package sewm.bdbox.search;

import java.util.Date;

import lombok.Data;

@Data
public class InfomallDocument {
  private String filename;
  private Long position;
  private String version;
  private String url;
  private String host;
  private Date date;
  private byte[] unzipBytes;
  private String html;
  private String charset;
  private String title;
  private String content;

  public InfomallDocument(String filename, Long position, String version,
      String url, String host, Date date, byte[] unzipBytes, String html,
      String charset, String title, String content) {
    this.filename = filename;
    this.position = position;
    this.version = version;
    this.url = url;
    this.host = host;
    this.date = date;
    this.unzipBytes = unzipBytes;
    this.html = html;
    this.charset = charset;
    this.title = title;
    this.content = content;
  }
}