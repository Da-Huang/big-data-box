package sewm.bdbox.search;

import java.util.Date;

import lombok.Data;

@Data
public class InfomallDocument {
  private String version;
  private String url;
  private Date date;
  private String data;
  private Long position;
  private String charset;
  private String title;
  private String content;
  private String path;
  private String host;

  public InfomallDocument(String version, String url, Date date, String data,
      Long position, String charset, String title, String content, String path,
      String host) {
    this.version = version;
    this.url = url;
    this.date = date;
    this.data = data;
    this.position = position;
    this.charset = charset;
    this.title = title;
    this.content = content;
    this.path = path;
    this.host = host;
  }
}
