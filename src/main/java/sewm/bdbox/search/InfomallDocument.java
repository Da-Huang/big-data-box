package sewm.bdbox.search;

import java.util.Date;

public class InfomallDocument {
  String version;
  String url;
  Date date;
  String data;
  Long position;
  String charset;
  String title;
  String content;
  public InfomallDocument(String version, String url, Date date, String data,Long position,String charset,String title,String content) {
    this.version = version;
    this.url = url;
    this.date = date;
    this.data = data;
    this.position = position;
    this.charset = charset;
    this.title = title;
    this.content = content;
  }

}
