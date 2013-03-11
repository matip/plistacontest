package de.dailab.plistacontest.recommender;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dailab.plistacontest.helper.LuceneUtil;

public class LuceneRecommender implements ContestRecommender {

	private static Logger logger = LoggerFactory.getLogger(LuceneRecommender.class);
	static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
	// Directory index = new RAMDirectory();
	Directory index;
	IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_41, analyzer);

	@Override
	public List<ContestItem> recommend(String _client, String _item, String _domain, String _description, String _limit) {
		final List<ContestItem> recList = new ArrayList<ContestItem>();

		System.out.println(_description);
		final JSONObject jObj = (JSONObject) JSONValue.parse(_description);
		String text = ((JSONObject) jObj.get("item")).get("text").toString();
		String id = ((JSONObject) jObj.get("item")).get("id").toString();
		String querystr = text;
		querystr = LuceneUtil.tokenizeString(analyzer, text).toString().replace("[", "").replace("]", "");
		Query q;

		try {
			q = new QueryParser(Version.LUCENE_41, "title", analyzer).parse(querystr);
			int hitsPerPage = 20;
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			// 4. display results
			System.out.println("Found " + hits.length + " hits.");

			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				if (!d.get("id").equals(id)) {
					recList.add(new ContestItem(Long.parseLong(d.get("id"))));
					System.out.println((i + 1) + ". " + d.get("id") + "\t" + d.get("title"));
					if (recList.size() == Integer.parseInt(_limit)) {
						break;
					}
				}
			}

			// reader can only be closed when there
			// is no need to access the documents any more.
			reader.close();
		} catch (ParseException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		}

		return recList;
	}

	public void init(final String _domain) throws IOException {
		logger.debug("INIT");
		index = FSDirectory.open(new File(_domain + "index"));
	}

	@Override
	public void impression(String _impression) {

		final JSONObject jObj = (JSONObject) JSONValue.parse(_impression);
		try {
			String text = ((JSONObject) jObj.get("item")).get("text").toString();
			String id = ((JSONObject) jObj.get("item")).get("id").toString();
			IndexWriter w;
			try {
				w = new IndexWriter(index, config);
				addDoc(w, text, new Date().toString(), id);
				w.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		} catch (NullPointerException e) {
			logger.info(e.toString());
		}

	}

	private static void addDoc(IndexWriter w, String text, String date, String id) throws IOException {
		Document doc = new Document();

		doc.add(new TextField("title", text, Field.Store.YES));
		doc.add(new TextField("date", date, Field.Store.YES));
		doc.add(new TextField("id", id.trim(), Field.Store.YES));
		w.updateDocument(new Term("id", id), doc);
	}

	@Override
	public void feedback(String _feedback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(String _error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setProperties(Properties properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

}
