package sewm.bdbox.search;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Test {
  public static void main(String[] args) throws Exception {
//    test5(new String[]{"a", "b", "c"});
//    test5("a", "b", "c", "d");
    test1();
  }

  protected static void test1() throws InterruptedException {
    // Runtime.getRuntime().addShutdownHook(new Thread() {
    // @Override
    // public void run() {
    // System.out.println("shit");
    // }
    // });
    Signal.handle(new Signal("INT"), new SignalHandler() {
      public void handle(Signal sig) {
        System.out.println("shit");
      }
    });
    Runnable r = new Runnable() {

      @Override
      public void run() {
        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        System.out.println("Closed " + Thread.currentThread().getId());
      }
    };
    ExecutorService executor = Executors.newFixedThreadPool(2);
    executor.execute(r);
    executor.execute(r);
    executor.shutdown();
    executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
  }

  protected static void test2() throws IOException {
    Analyzer analyzer = new SmartChineseAnalyzer(true);
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    RAMDirectory dir = new RAMDirectory();
    IndexWriter iw = new IndexWriter(dir, iwc);
    Document doc = new Document();
    doc.add(new StringField("data", "a", Store.YES));
    doc.add(new StringField("data", "b", Store.YES));
    iw.addDocument(doc);
    iw.close();
    IndexReader ir = DirectoryReader.open(dir);
    IndexSearcher is = new IndexSearcher(ir);
    TopDocs top = is.search(new TermQuery(new Term("data", "a")),
        Integer.MAX_VALUE);
    for (ScoreDoc scoreDoc : top.scoreDocs) {
      Document doc2 = is.doc(scoreDoc.doc);
      String[] data = doc2.getValues("data");
      for (String string : data) {
        System.out.println(string);
      }
    }
  }

  protected static void test3() throws IOException, ParseException {
    Analyzer analyzer = new SmartChineseAnalyzer(true);
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    RAMDirectory dir = new RAMDirectory();
    IndexWriter iw = new IndexWriter(dir, iwc);
    Document doc = new Document();
    doc.add(new StringField("data", "a", Store.YES));
    doc.add(new StringField("data", "b", Store.YES));
    doc.add(new TextField("data", "https://github.com/Da-Huang/big-data-box",
        Store.NO));
    TokenStream stream = analyzer.tokenStream("data",
        "https://github.com/Da-Huang/big-data-box");
    QueryParser parser = new QueryParser("x", analyzer);
    String str = "github.com/Da-Huang/big-data-box".replaceAll("[/?#.\\-_]",
        " ");
    // System.out.println(str);
    // System.out.println(parser.parse(str));
    iw.addDocument(doc);
    doc = new Document();
    doc.add(new StringField("data", "a", Store.YES));
    doc.add(new StringField("data", "d", Store.YES));
    iw.addDocument(doc);

    doc = new Document();
    doc.add(new StringField("data", "a", Store.YES));
    doc.add(new StringField("data", "t", Store.YES));
    iw.updateDocument(new Term("data", "a"), doc);

    iw.close();
    IndexReader ir = DirectoryReader.open(dir);
    IndexSearcher is = new IndexSearcher(ir);
    TopDocs top = is.search(new TermQuery(new Term("data", "a")),
        Integer.MAX_VALUE);
    for (ScoreDoc scoreDoc : top.scoreDocs) {
      Document doc2 = is.doc(scoreDoc.doc);
      String[] data = doc2.getValues("data");
      for (String string : data) {
        System.out.print(string + " ");
      }
      System.out.println();
    }
  }
  
  public static void test4() {
    Date date = InfomallDocumentIterator.parseDate("Sat, 21 Feb 2009 17:01:02 GMT");
    System.out.println(date);
  }
  
  public static void test5(String... args) {
    for (String arg : args) {
      System.out.println(arg);
    }
  }
}
