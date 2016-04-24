package sewm.bdbox.util;

import java.io.Writer;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import sewm.bdbox.search.InfomallDocument;
import sewm.bdbox.search.InfomallSearcher;

public class InfomallSearchUtil {
  public static void writeResultAsJson(Writer writer, String infomallDataRoot,
      InfomallSearcher searcher, TopDocs top) {
    JsonGenerator generator = Json.createGenerator(writer);
    generator.writeStartObject().write("total_hits", top.totalHits)
        .writeStartArray("docs");
    for (ScoreDoc scoreDoc : top.scoreDocs) {
      Document doc = searcher.doc(scoreDoc.doc);
      InfomallDocument infomallDoc = InfomallFetchUtil.fetch(infomallDataRoot,
          doc.get("filename"), doc.get("position"));

      generator.writeStartObject().write("doc_id", scoreDoc.doc)
          .write("title", infomallDoc.getTitle())
          .write("url", infomallDoc.getUrl())
          .write("date", infomallDoc.getDate().getTime())
          .write("length", infomallDoc.getUnzipBytes().length).writeEnd();
    }
    generator.writeEnd().writeEnd();
  }
}
