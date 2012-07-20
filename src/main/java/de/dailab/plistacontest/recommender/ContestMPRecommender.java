package de.dailab.plistacontest.recommender;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dailab.plistacontest.helper.DataModelHelper;
import de.dailab.plistacontest.helper.DateHelper;
import de.dailab.plistacontest.helper.FalseItems;
import de.dailab.plistacontest.helper.MahoutWriter;

/**
 * Most Popular Recoommender for the plista contest
 * 
 * @author till
 * 
 */
public class ContestMPRecommender
                implements ContestRecommender {

    private static Logger logger = LoggerFactory.getLogger(ContestMPRecommender.class);

    public volatile AbstractRecommender recommender;

    private FalseItems falseItems = new FalseItems();

    private DataModel dataModel;

    private int impressionCount = 30;

    private int impressionCounter = 0;

    private List<ContestItem> lastResponseCache = null;

    /**
     * Constructor - calls init method.
     */
    public ContestMPRecommender() {
        init();
    }

    private void init() {

        try {
            this.dataModel = DataModelHelper.getDataModel(5);
            this.recommender = new MostPopularItemsRecommender(this.dataModel);
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }
        catch (TasteException e) {
            logger.error(e.getMessage());
        }
        // load false items
        deserialize();
    }

    public List<ContestItem> recommend(String _client, String _item, String _description, String _limit) {
        final List<ContestItem> recList = new ArrayList<ContestItem>();
        try {
            final AbstractRecommender tmpRec;

            synchronized (this) {
                tmpRec = this.recommender;
            }

            final List<RecommendedItem> tmp = tmpRec.recommend(Long.parseLong(_client), Integer.parseInt(_limit),
                            new MPRescorer(this.falseItems));

            for (RecommendedItem recommendedItem : tmp) {
                recList.add(new ContestItem(recommendedItem.getItemID()));
            }
        }
        catch (TasteException e) {
            logger.error(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        return recList;
    }

    public void impression(String _impression) {
        // write info directly in MAHOUT format
        new Thread(new MahoutWriter("m_data_" + DateHelper.getDate() + ".txt", _impression, 3)).start();

        this.impressionCounter++;
        // update the model after X impressions
        if (this.impressionCounter >= this.impressionCount) {

            this.impressionCounter = 0;
            new Thread() {

                public void run() {
                    update();
                }

            }.start();

        }
    }

    private void update() {
        AbstractRecommender recommender = null;
        try {
            recommender = new MostPopularItemsRecommender(DataModelHelper.getDataModel(5));
        }
        catch (TasteException e) {
            logger.error(e.getMessage());
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }

        if (recommender != null) {
            synchronized (this) {
                this.recommender = recommender;
            }
        }
    }

    @Override
    public void feedback(String _feedback) {
        try {
            final JSONObject jObj = (JSONObject) JSONValue.parse(_feedback);
            final String client = ((JSONObject) jObj.get("client")).get("id").toString();
            final String item = ((JSONObject) jObj.get("target")).get("id").toString();
            // write info directly in MAHOUT format -> with pref 5
            new Thread(new MahoutWriter("m_data_" + DateHelper.getDate() + ".txt", client + "," + item, 5)).start();
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

	@Override
	public void error(String _error) {
		logger.error(_error);
		// {"error":"invalid items returned:89194287","team":{"id":"65"},"code":null,"version":"1.0"}
		try {
			final JSONObject jErrorObj = (JSONObject) JSONValue.parse(_error);
			if (jErrorObj.containsKey("error")) {
				String error = jErrorObj.get("error").toString();
				if (error.contains("invalid items returned:")) {
					String tmpError = error.replace("invalid items returned:", "");
					String[] errorItems = tmpError.split(",");
					for (String errorItem : errorItems) {
						this.falseItems.addItem(Long.parseLong(errorItem));
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		serialize(this.falseItems);
	}

    private void serialize(final FalseItems _falseItemse) {
        try {
            final FileOutputStream fileOut = new FileOutputStream("falseitems.ser");
            final ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(_falseItemse);
            out.close();
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void deserialize() {
        try {
            final FileInputStream fileIn = new FileInputStream("falseitems.ser");
            final ObjectInputStream in = new ObjectInputStream(fileIn);
            this.falseItems = (FalseItems) in.readObject();
            in.close();
            fileIn.close();
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }
        catch (ClassNotFoundException e1) {
            logger.error(e1.getMessage());
        }
    }

}
