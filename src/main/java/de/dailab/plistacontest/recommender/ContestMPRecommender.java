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

    private Logger logger = LoggerFactory.getLogger(ContestMPRecommender.class);

    public AbstractRecommender recommender;

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
            this.dataModel = DataModelHelper.getDataModel();
            this.recommender = new MostPopularItemsRecommender(dataModel);
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
            final List<RecommendedItem> tmp = this.recommender.recommend(Integer.parseInt(_client),
                            Integer.parseInt(_limit), new MPRescorer(this.falseItems));
            for (RecommendedItem recommendedItem : tmp) {
                recList.add(new ContestItem(recommendedItem.getItemID()));
            }
        }
        catch (TasteException e) {
            this.logger.error(e.getMessage());
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

            try {
                this.recommender = new MostPopularItemsRecommender(DataModelHelper.getDataModel());
            }
            catch (TasteException e) {
                logger.error(e.getMessage());
            }
            catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

    }

    @Override
    public void feedback(String _feedback) {
        // TODO Auto-generated method stub

    }

    @Override
    public void error(final String _error) {
        
        final JSONObject jObj = (JSONObject) JSONValue.parse(_error);
        if (jObj.get("error").toString().contains("mismatch or invalid items")) {
            for (ContestItem contestItem : this.lastResponseCache) {
                this.falseItems.addItem(contestItem.getId());
            }

            serialize(this.falseItems);
        }

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
