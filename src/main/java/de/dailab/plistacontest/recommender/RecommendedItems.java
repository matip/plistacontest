package de.dailab.plistacontest.recommender;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RecommendedItems
                implements Serializable {

    private static final long serialVersionUID = 2612687932321938665L;

    private Map<Long, Map<Long, Integer>> recommended = new HashMap<Long, Map<Long, Integer>>();

    public RecommendedItems() {
        super();
    }

    /**
     * @param _userId
     * @param _itemId
     * @return
     */
    public boolean isAlreadyRecommended(final long _userId, final long _itemId) {
        boolean isRec = false;
        if (this.recommended.get(_userId) != null && _userId != 0) {
            // recommend again if item is not recommended more than 3 times
            if (this.recommended.get(_userId).containsKey(_itemId) && this.recommended.get(_userId).get(_itemId) > 3) {
                isRec = true;
            }
        }

        return isRec;
    }

    public void addItem(final long _userId, final long _itemId) {
        if (this.recommended.get(_userId) != null) {
            if (this.recommended.get(_userId).containsKey(_itemId)) {
                final Map<Long, Integer> userItems = this.recommended.get(_userId);
                userItems.put(_itemId, userItems.get(_itemId) + 1);
                this.recommended.put(_userId, this.recommended.put(_userId, userItems));
            }

        }
        else {
            final Map<Long, Integer> l = new HashMap<Long, Integer>();
            l.put(_userId, 1);
            this.recommended.put(_userId, l);
        }
    }

}
