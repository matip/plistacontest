package de.dailab.plistacontest.recommender;

import java.util.List;

import de.dailab.plistacontest.client.ContestHandler;

/**
 * @author till
 *
 */
public interface ContestRecommender {

	
	/**
	 * Recommend method called by {@link ContestHandler} 
	 * 
	 * @param _client
	 * @param _item
	 * @param _description
	 * @param _limit
	 * @return
	 */
	public List<ContestItem> recommend(final String _client, final String _item, final String _description, final String _limit);
	
	/**
	 * Impression Information from the Plista server is send to this method
	 * 
	 * @param _impression
	 */
	public void impression(final String _impression);
	
	/**
     * Impression Information from the Plista server is send to this method
     * 
     * @param _feedback
     */
	public void feedback(final String _feedback);
	
	/**
     * Impression Information from the Plista server is send to this method
     * 
     * @param _error
     */
	public void error(final String _error);
}
