package de.dailab.plistacontest.recommender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContestLuceneRecommender implements ContestRecommender {

	private static Logger logger = LoggerFactory.getLogger(ContestLuceneRecommender.class);

	// recommender map
	private final Map<String, LuceneRecommender> domainRecommender = new HashMap<String, LuceneRecommender>();


	@Override
	public List<ContestItem> recommend(String _client, String _item, String _domain, String _description, String _limit) {
		final List<ContestItem> recList = new ArrayList<ContestItem>();
		try {
			final LuceneRecommender tmpRec;

			synchronized (this) {
				tmpRec = this.domainRecommender.get(_domain);
			}

			recList.addAll(tmpRec.recommend(_client, _item, _domain, _description, _limit));

		} catch (Exception e) {
			logger.error(e.toString() + " DOMAIN: " + _domain);
		}
		return recList;
	}


	@Override
	public void init() {
		logger.debug("INIT");
	}

	@Override
	public void impression(String _impression) {
		
		final JSONObject jObj = (JSONObject) JSONValue.parse(_impression);
		final String domain = ((JSONObject) jObj.get("domain")).get("id").toString();
		
		if(this.domainRecommender.containsKey(domain)) {
			this.domainRecommender.get(domain).impression(_impression);
		} else {
			this.domainRecommender.put(domain, new LuceneRecommender());
			try {
				this.domainRecommender.get(domain).init(domain);
				this.domainRecommender.get(domain).impression(_impression);
			} catch (IOException e) {
				logger.error(e.toString());			}
		}
		
		

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

}
