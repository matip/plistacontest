package de.dailab.plistacontest.recommender;

public class ContestItem {

	long id;
	
	public ContestItem(final long _id) {
		this.id = _id;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	
}
