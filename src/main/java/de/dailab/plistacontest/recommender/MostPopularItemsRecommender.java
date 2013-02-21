package de.dailab.plistacontest.recommender;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dailab.plistacontest.helper.DateHelper;

public class MostPopularItemsRecommender extends AbstractRecommender {

	private final Logger logger = LoggerFactory.getLogger(MostPopularItemsRecommender.class);

	private final List<Entry<Long, Double>> mostPopluarItems = new ArrayList<Map.Entry<Long, Double>>();

	private RecommendedItems recommendedItems = new RecommendedItems();

	private String domain;

	public String getDomain() {
		return domain;
	}

	/**
	 * @param _dataModel
	 * @param _timeboost
	 * @param _domain
	 * 
	 * */
	public MostPopularItemsRecommender(final DataModel _dataModel, final boolean _timeboost, final String _domain)
			throws TasteException {

		super(_dataModel);

		// process the most popular items of the dataset amongst all users
		final List<Entry<Long, Double>> sortedList = sortByValue(countItems(_timeboost));
		// revert list, so that the item with the most entries is on top>
		Collections.reverse(sortedList);
		this.mostPopluarItems.addAll(sortedList);
		this.domain = _domain;

		deserialize();
	}

	/**
	 * @param _dataModel
	 * 
	 * */
	public MostPopularItemsRecommender(final DataModel _dataModel) throws TasteException {
		this(_dataModel, false, "notSet");
	}

	/**
	 * Counts all items in the dataset.
	 * 
	 * @param _timeaware
	 *            if true, current items are boosted
	 * @return
	 * @throws TasteException
	 */
	private Map<Long, Double> countItems(final boolean _timeaware) throws TasteException {

		final LongPrimitiveIterator lpi = this.getDataModel().getUserIDs();

		final Map<Long, Double> map = new TreeMap<Long, Double>();

		// iterate over users
		while (lpi.hasNext()) {
			long uid = lpi.next();
			final FastIDSet uili = this.getDataModel().getItemIDsFromUser(uid);
			final LongPrimitiveIterator iIter = uili.iterator();

			// iterate over the items
			while (iIter.hasNext()) {

				long itemId = iIter.next();

				double boostfactor = 1;
				if (_timeaware) {
					final Long timestamp = this.getDataModel().getPreferenceTime(uid, itemId);
					if (timestamp != null) {
						final Date time = new java.util.Date((long) timestamp);
						//boost more current impressions
						boostfactor = getBoostFactor(time);
					}
				}
				
				// count items
				if (map.containsKey(itemId)) {
					map.put(itemId, map.get(itemId) +  boostfactor);
				} else {
					map.put(itemId, 1d * boostfactor);
				}
			}
		}

		return map;
	}

	private int getBoostFactor(final Date _itemTimeStamp) {
		int boost = 1;
		try {

			// letzte Stunde
			if (DateHelper.getDateWithHours(-1).compareTo(_itemTimeStamp) < 0) {
				boost = 80;
			} else if (DateHelper.getDateWithHours(-2).compareTo(_itemTimeStamp) < 0) {
			// letzten 2 Stunden
				boost = 60;
			} else if (DateHelper.getDateWithHours(-4).compareTo(_itemTimeStamp) < 0) {
				// letzten 4 Stunden
				boost = 40;
			} else if (formatDate(_itemTimeStamp).equals(DateHelper.getDate())) {
				//today
				boost = 30;
			} else if (formatDate(_itemTimeStamp).equals(DateHelper.getYesterday())) {
				//yesterday
				boost = 15;
			}
		} catch (Exception e) {
			this.logger.error(e.toString());
		}
		return boost;
	}

	private String formatDate(Date _d) throws ParseException {
		final DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		return formatter.format(_d);
	}

	public List<RecommendedItem> recommend(long userID, int howMany, IDRescorer rescorer) throws TasteException {
		FastIDSet idSet = new FastIDSet();

		try {
			idSet = this.getDataModel().getItemIDsFromUser(userID);
		} catch (org.apache.mahout.cf.taste.common.NoSuchUserException e) {
			this.logger.debug(e.toString());
		}

		final List<RecommendedItem> recommendedItems = new ArrayList<RecommendedItem>();

		int listKey = 0;
		for (int i = 0; i < howMany; i++) {
			if (i < this.mostPopluarItems.size()) {
				long itemId = this.mostPopluarItems.get(listKey).getKey();

				if (!idSet.contains(itemId) && !rescorer.isFiltered(itemId)
						&& !this.recommendedItems.isAlreadyRecommended(userID, itemId)) {
					RecommendedItem recommendedItem = new GenericRecommendedItem(itemId, 5);
					recommendedItems.add(recommendedItem);
					this.recommendedItems.addItem(userID, itemId);

				} else {
					i--;
				}
				listKey++;
			} else {
				break;
			}
		}

		serialize();
		return recommendedItems;
	}

	private void serialize() {
		try {
			final FileOutputStream fileOut = new FileOutputStream(this.domain + "_recommendedItems.ser");
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this.recommendedItems);
			out.close();
		} catch (IOException e) {
			logger.error(e.toString());
		}
	}

	private void deserialize() {
		try {
			FileInputStream fileIn = new FileInputStream(this.domain + "_recommendedItems.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			this.recommendedItems = (RecommendedItems) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException e) {
			logger.error(e.toString());
		} catch (ClassNotFoundException e1) {
			logger.error(e1.toString());
		}
	}

	public <K, V extends Comparable<V>> List<Entry<K, V>> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(map.entrySet());
		Collections.sort(entries, new ByValue<K, V>());
		return entries;
	}

	private class ByValue<K, V extends Comparable<V>> implements Comparator<Entry<K, V>> {

		public int compare(Entry<K, V> o1, Entry<K, V> o2) {
			if (o1.getValue().compareTo(o2.getValue()) == 0) {
				try {
					if (Long.getLong(o1.getKey().toString()) < (Long.getLong(o2.getKey().toString()))) {
						return -1;
					} else {
						return 1;
					}
				} catch (Exception e) {
					return 0;
				}

			} else {
				return o1.getValue().compareTo(o2.getValue());
			}

		}
	}

	public float estimatePreference(long userID, long itemID) throws TasteException {
		return 0;
	}

	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		// TODO Auto-generated method stub
	}
}
