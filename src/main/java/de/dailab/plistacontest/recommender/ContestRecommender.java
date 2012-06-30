package de.dailab.plistacontest.recommender;

import java.util.List;

/**
 * @author till
 *
 */
public interface ContestRecommender {

	
	public List<ContestItem> recommend(final String _client, final String _item, final String _limit);
	
	public List<ContestItem> recommend(final String _client, final String _item, final String _description, final String _limit);
}
