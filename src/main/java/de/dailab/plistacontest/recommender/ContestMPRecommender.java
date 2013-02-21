package de.dailab.plistacontest.recommender;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
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

    // recommender map
    private final Map<String, AbstractRecommender> domainRecommender = new HashMap<String, AbstractRecommender>();

    private FalseItems falseItems = new FalseItems();

    // number of impressions before a recommender is updated
    private int impressionCount = 30;

    // number of days taken into account for the data model
    private int numberOfDays = 5;

    // don't recommend items after they were marked as invalid n times
    private int ignoreAfter = 2;

    private Properties properties = new Properties();

    // multicounter for different recommender
    final Map<String, Integer> counter = new HashMap<String, Integer>();

    public void init() {
        // set properties
        this.impressionCount = Integer.parseInt(properties.getProperty("plista.impressionCount", "30"));
        this.numberOfDays = Integer.parseInt(properties.getProperty("plista.numOfDays", "5"));

        // get all data files for the different domains
        final File dir = new File(".");
        final FileFilter fileFilter = new WildcardFileFilter("*_m_data*.txt");
        final File[] files = dir.listFiles(fileFilter);

        // get domains
        List<String> domains = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            final String domainFile = files[i].getName();
            final String domain = domainFile.substring(0, domainFile.indexOf("_"));
            if (!domains.contains(domain)) {
                domains.add(domain);
            }
        }

        // create domain MP Recommender
        for (String d : domains) {
            try {
                this.domainRecommender.put(
                                d,
                                new MostPopularItemsRecommender(DataModelHelper.getDataModel(this.numberOfDays, d),
                                                Boolean.parseBoolean(properties
                                                                .getProperty("plista.timeBoost", "false")),d));
            }
            catch (IOException e) {
                logger.error(e.getMessage());
            }
            catch (TasteException e) {
                logger.error(e.getMessage());
            }
        }

        // load false items
        deserialize();
    }

    public List<ContestItem> recommend(String _client, String _item, String _domain, String _description, String _limit) {
        final List<ContestItem> recList = new ArrayList<ContestItem>();
        try {
            final AbstractRecommender tmpRec;

            synchronized (this) {
                tmpRec = this.domainRecommender.get(_domain);
            }

            final List<RecommendedItem> tmp = tmpRec.recommend(Long.parseLong(_client), Integer.parseInt(_limit),
                            new MPRescorer(this.falseItems, this.ignoreAfter));

            for (RecommendedItem recommendedItem : tmp) {
                recList.add(new ContestItem(recommendedItem.getItemID()));
            }
        }
        catch (TasteException e) {
            logger.error(e.toString());
        }
        catch (Exception e) {
            logger.error(e.toString() + " DOMAIN: " + _domain);
        }
        return recList;
    }

    public void impression(String _impression) {

        final JSONObject jObj = (JSONObject) JSONValue.parse(_impression);
        final String domain = ((JSONObject) jObj.get("domain")).get("id").toString();

        // write info directly in MAHOUT format
        new Thread(new MahoutWriter(domain + "_m_data_" + DateHelper.getDate() + ".txt", _impression, 3)).start();

        // update impression counter
        if (this.counter.containsKey(domain)) {
            this.counter.put(domain, this.counter.get(domain) + 1);
        }
        else {
            this.counter.put(domain, 1);
        }

        if (this.counter.get(domain) >= this.impressionCount) {
            this.counter.put(domain, 0);
            new Thread() {

                public void run() {
                    update(domain);
                }

            }.start();
        }
    }

    private void update(final String _domain) {
        AbstractRecommender recommender = null;
        try {
            recommender = new MostPopularItemsRecommender(DataModelHelper.getDataModel(this.numberOfDays, _domain),
                            Boolean.parseBoolean(properties.getProperty("plista.timeBoost", "false")), _domain);
        }
        catch (TasteException e) {
            logger.error(e.getMessage());
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }

        if (recommender != null) {
            synchronized (this) {
                this.domainRecommender.put(_domain, recommender);
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
        }
        catch (Exception e) {
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

    /**
     * @param properties
     *            the properties to set
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
