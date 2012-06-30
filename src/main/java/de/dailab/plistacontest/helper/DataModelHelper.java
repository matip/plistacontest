package de.dailab.plistacontest.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SequenceInputStream;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericItemPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author till
 * 
 */
public final class DataModelHelper {

    private static Logger logger = LoggerFactory.getLogger(DataModelHelper.class);

    /**
     * Creates an aggregated data model with the data from today and the last 3 days. The names of the file must follow
     * the structure m_data_YYYYmmdd.txt
     * 
     * @return
     * @throws IOException
     */
    public static DataModel getDataModel()
                    throws IOException {

        final String[] files = new String[] { "m_data_" + DateHelper.getDate() + ".txt",
                        "m_data_" + DateHelper.getYesterday() + ".txt", "m_data_" + DateHelper.getTDBY() + ".txt",
                        "m_data_" + DateHelper.getDateBefore(-3) + ".txt" };

        return getDataModel(files);
    }

    /**
     * Creates an aggregated data model from the given files.
     * 
     * @return
     * @throws IOException
     */
    public static DataModel getDataModel(final String[] _files)
                    throws IOException {

        final SequenceInputStream sis = new SequenceInputStream(new DataEnumeration(_files));
        final File file = File.createTempFile("datamodel", "tmp");
        file.deleteOnExit();

        final FileOutputStream fostream = new FileOutputStream(file);

        int temp;
        while ((temp = sis.read()) != -1) {
            fostream.write(temp); // to write to file
        }

        DataModel dataModel = null;
        try {
            dataModel = new FileDataModel(file);
        }
        catch (Exception e1) {
            logger.error(e1.getMessage());
        }

        // if the file data model is empty and thus not created, create an intial data model with test data.
        if (dataModel == null) {
            FastByIDMap<PreferenceArray> userData = new FastByIDMap<PreferenceArray>(1);
            Preference p = new GenericPreference(1, 1, 1);
            PreferenceArray array = new GenericItemPreferenceArray(1);
            array.set(0, p);
            userData.put(1, array);
            dataModel = new GenericDataModel(userData);
        }

        try {
            logger.debug("No. of items: " + dataModel.getNumItems());
        }
        catch (TasteException e) {
            // ignore
        }
        fostream.close();
        sis.close();

        return dataModel;
    }

}
