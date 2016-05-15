package sewm.bdbox.search;

import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import lombok.Data;

@Data
public class InfomallDocument {
  // Required.
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

  // Optional.
  private String ip;
  private String origin;
  private String referer;
  // Key: Anchor Text, Value: Raw URL.
  private List<Entry<String, String>> anchors;
}