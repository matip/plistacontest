package de.dailab.plistacontest.recommender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dailab.plistacontest.helper.DataModelHelper;
import de.dailab.plistacontest.helper.FalseItems;

/**
 *
 * 
 * @author till
 * 
 */
public class ContestMPRecommender implements ContestRecommender {

	private Logger logger = LoggerFactory.getLogger(ContestMPRecommender.class);
	public AbstractRecommender recommender;
	private FalseItems falseItems;
	private DataModel dataModel;

	public ContestMPRecommender(final FalseItems _falseItems) {
		this.falseItems = _falseItems;
		init();
	}

	/**
	 * Constructor - calls init method.
	 */
	public ContestMPRecommender() {
		init();
	}

	private void init() {

		try {
			this.dataModel = DataModelHelper.getDataModel();
			this.recommender = new MostPopularItemsRecommender(dataModel);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TasteException e) {
			e.printStackTrace();
		}
	}

	public List<ContestItem> recommend(final String _client, final String _item, final String _limit) {

		final List<ContestItem> recList = new ArrayList<ContestItem>();
		try {
			final List<RecommendedItem> tmp = this.recommender.recommend(Integer.parseInt(_client),
					Integer.parseInt(_limit), new MPRescorer(this.falseItems));
			for (RecommendedItem recommendedItem : tmp) {
				recList.add(new ContestItem(recommendedItem.getItemID()));

			}

		} catch (TasteException e) {
			this.logger.error(e.getMessage());
		}

		return recList;
	}

	public List<ContestItem> recommend(String _client, String _item,
			String _description, String _limit) {
		return recommend(_client, _item, _limit);
	}

}
