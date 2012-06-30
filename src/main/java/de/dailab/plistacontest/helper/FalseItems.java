package de.dailab.plistacontest.helper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author till
 * 
 */
public class FalseItems implements Serializable {

	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = 1874661976007272323L;

	private Map<Long, Integer> falseItems = new HashMap<Long, Integer>();

	/**
	 * Adds items to the false items list. Either creates a new entry or
	 * increments the count.
	 * 
	 * @param _itemId
	 *            id of the item marked as invalid
	 */
	public void addItem(final Long _itemId) {
		if (this.falseItems.containsKey(_itemId)) {
			this.falseItems.put(_itemId, this.falseItems.get(_itemId) + 1);
		} else {
			this.falseItems.put(_itemId, 1);
		}
	}

	/**
	 * Method to check if an item is in the list.
	 * 
	 * @param _itemId
	 *            id of the item to check
	 * @return true if the item is in the list.
	 */
	public boolean containsItem(final Long _itemId) {
		return this.falseItems.containsKey(_itemId);
	}

	/**
	 * Returns the count of the item, -1 if the item is not in the list.
	 * 
	 * @param _itemId
	 *            id of the item to check
	 * @return count of the item
	 */
	public int getItemCount(final Long _itemId) {
		if (containsItem(_itemId)) {
			return this.falseItems.get(_itemId);
		}

		return -1;
	}

	/**
	 * @return the falseItems
	 */
	public Map<Long, Integer> getFalseItems() {
		return falseItems;
	}

	/**
	 * @param falseItems
	 *            the falseItems to set
	 */
	public void setFalseItems(final Map<Long, Integer> _falseItems) {
		this.falseItems = _falseItems;
	}

}
