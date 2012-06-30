package de.dailab.plistacontest.recommender;

import org.apache.mahout.cf.taste.recommender.IDRescorer;

import de.dailab.plistacontest.helper.FalseItems;

public class MPRescorer implements IDRescorer {

	private FalseItems falseItems;
	// max count for items in the false items list. If an item is more often in
	// the list, isFiltered will return true.
	private int maxCount = 4;

	public MPRescorer(final FalseItems _falseItems) {
		this.falseItems = _falseItems;
	}

	public MPRescorer(final FalseItems _falseItems, final int _maxCount) {
		this.falseItems = _falseItems;
		this.maxCount = _maxCount;

	}

	public double rescore(long id, double originalScore) {
		return 0;
	}

	public boolean isFiltered(final long _id) {
		boolean isFiltered = false;
		try {
			if (this.falseItems != null && this.falseItems.containsItem(_id)
					&& this.falseItems.getItemCount(_id) >= this.maxCount) {
				isFiltered = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isFiltered;
	}

}
