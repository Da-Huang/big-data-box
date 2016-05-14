package sewm.bdbox.util;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.print.Doc;
import javax.swing.text.StyledEditorKit.ForegroundAction;
import javax.swing.text.html.HTML;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import sewm.bdbox.search.InfomallDocument;
import sewm.bdbox.search.InfomallSearcher;

// TODO: Take Origin into concern.
public class InfomallGraphUtil {
  /**
   * Returns <Normalized URL, URL>.
   */
  public static Map<String, String> fetchInUrls(
      final Map<String, String> dataMap, InfomallSearcher searcher,
      String url) {
    Map<String, String> ans = new HashMap<>();
    TopDocs top = searcher.search(
        new TermQuery(new Term("anchor_url", HtmlUtil.normalizeURL(url))),
        Integer.MAX_VALUE);
    for (ScoreDoc scoreDoc : top.scoreDocs) {
      Document doc = searcher.doc(scoreDoc.doc);
      final String filename = doc.get("filename");
      final String position = doc.get("position");
      InfomallDocument infomallDoc = InfomallFetchUtil.fetch(dataMap, filename,
          position);
      if (infomallDoc != null) {
        String normalizedUrl = HtmlUtil.normalizeURL(infomallDoc.getUrl());
        if (!ans.containsKey(normalizedUrl)) {
          ans.put(normalizedUrl, infomallDoc.getUrl());
        }
      }
    }
    return ans;
  }

  /**
   * Returns <Normalized URL, URL>.
   */
  public static Map<String, String> fetchOutUrls(
      final Map<String, String> dataMap, InfomallSearcher searcher,
      String url) {
    Map<String, String> ans = new HashMap<>();
    TopDocs top = searcher.search(
        new TermQuery(new Term("url", HtmlUtil.normalizeURL(url))),
        Integer.MAX_VALUE);
    for (ScoreDoc scoreDoc : top.scoreDocs) {
      Document doc = searcher.doc(scoreDoc.doc);
      final String filename = doc.get("filename");
      final String position = doc.get("position");
      InfomallDocument infomallDoc = InfomallFetchUtil.fetch(dataMap, filename,
          position);
      if (infomallDoc != null && infomallDoc.getAnchors() != null) {
        for (Entry<String, String> anchor : infomallDoc.getAnchors()) {
          String normalizedAnchorUrl = HtmlUtil.normalizeURL(anchor.getValue());
          if (!ans.containsKey(normalizedAnchorUrl)) {
            ans.put(normalizedAnchorUrl, anchor.getValue());
          }
        }
      }
    }
    return ans;
  }

  public static void writeResultAsJson(Writer writer, String url,
      final Map<String, String> in, final Map<String, String> out) {
    JsonGenerator generator = Json.createGenerator(writer);
    generator.writeStartObject().writeStartObject("node").write("url", url)
        .write("normalized_url", HtmlUtil.normalizeURL(url)).writeEnd();

    generator.writeStartArray("in");
    for (Entry<String, String> entry : in.entrySet()) {
      generator.writeStartObject().write("url", entry.getValue())
          .write("normalized_url", entry.getKey());
    }
    generator.writeEnd().writeStartArray("out");
    for (Entry<String, String> entry : out.entrySet()) {
      generator.writeStartObject().write("url", entry.getValue())
          .write("normalized_url", entry.getKey());
    }
    generator.writeEnd().writeEnd().close();
  }
}