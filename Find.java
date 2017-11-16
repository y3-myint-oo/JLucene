
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;

/**
 * This terminal application creates an Apache Lucene index in a folder and adds
 * files into this index based on the input of the user.
 */
public class Find {
	private static StandardAnalyzer analyzer = new StandardAnalyzer();
	private IndexWriter writer;
	private ArrayList<File> queue = new ArrayList<File>();

	public Find(String dira) {
		try {
			FSDirectory dir = FSDirectory.open(Paths.get(dira));
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			writer = new IndexWriter(dir, config);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws IOException {
		Find indexer = new Find("c:\\tmp\\index");
		indexer.indexFileOrDirectory("E:\\RADWS\\Lucene\\Libs");
		indexer.closeIndex();

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("c:\\tmp\\index")));
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(5);

		try {
			Query q = new QueryParser("contents", analyzer).parse("ModelFactory.class");
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				// d.get("contents")
				// System.out.println(" "+ d.get("contents"));
				System.out.println(" Document Path " + d.get("path") + " File Name : " + d.get("filename") + " rate :"
						+ hits[i].score);
				findLineNumber(d.get("path").toString(), "ModelFactory.class");
			}

		} catch (Exception e) {
			System.out.println("Error searching " + e.getMessage());
		}
	}

	public void indexFileOrDirectory(String fileName) throws IOException {
		addFiles(new File(fileName));
		int originalNumDocs = writer.numDocs();
		for (File f : queue) {
			FileReader fr = null;
			try {
				Document doc = new Document();
				fr = new FileReader(f);
				doc.add(new TextField("contents", fr));
				doc.add(new StringField("path", f.getPath(), Field.Store.YES));
				doc.add(new StringField("filename", f.getName(), Field.Store.YES));

				writer.addDocument(doc);
				System.out.println("Added: " + f);
			} catch (Exception e) {
				System.out.println("Could not add: " + f);
			} finally {
				fr.close();
			}
		}

		int newNumDocs = writer.numDocs();
		System.out.println("");
		System.out.println("************************");
		System.out.println((newNumDocs - originalNumDocs) + " documents added.");
		System.out.println("************************");

		queue.clear();
	}

	private void addFiles(File file) {

		if (!file.exists()) {
			System.out.println(file + " does not exist.");
		}
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				addFiles(f);
			}
		} else {
			String filename = file.getName().toLowerCase();
			// ===================================================
			// Only index text files
			// ===================================================
			if (filename.endsWith(".htm") || filename.endsWith(".html") || filename.endsWith(".xml")
					|| filename.endsWith(".txt") || filename.endsWith(".xan")) {
				queue.add(file);
			} else {
				System.out.println("Skipped " + filename);
			}
		}
	}

	/**
	 * Close the index.
	 * 
	 * @throws java.io.IOException
	 *             when exception closing
	 */
	public void closeIndex() throws IOException {
		writer.close();
	}

	public static void findLineNumber(String path, String className) {
		File file = new File(path);
		String importPackage = "";
		String line = "";
		try {
			FileReader fw = new FileReader(file);
			BufferedReader bw = new BufferedReader(fw);
			while ((line = bw.readLine()) != null) {
				if (line.trim().endsWith("/" + className)) {
					importPackage = line.replaceAll("/", "\\.").substring(0, line.length() - 6);
					System.out.println(importPackage);
				//	break;
				}
			}
			bw.close();
			fw.close();
			System.out.println("Done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}