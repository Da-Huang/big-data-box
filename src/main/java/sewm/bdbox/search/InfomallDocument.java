package sewm.bdbox.search;

import java.util.Date;

public class InfomallDocument {
  String version;
  String url;
  Date date;
  String data;
  public InfomallDocument(String version, String url, Date date, String data) {
    this.version = version;
    this.url = url;
    this.date = date;
    this.data = data;
  }

}
