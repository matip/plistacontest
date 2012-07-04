package de.dailab.plistacontest.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MahoutWriter
                implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(MahoutWriter.class);

    private final String fileName;

    private final String impression;

    private final int rating;

    private final boolean json;

    public MahoutWriter(final String _fileName, String _impression, int _rating) {
        this(_fileName, _impression, _rating, true);

    }

    public MahoutWriter(final String _fileName, String _impression, int _rating, final boolean _json) {
        this.fileName = _fileName;
        this.impression = _impression;
        this.rating = _rating;
        this.json = _json;

    }

    public void writeImpressions() {
        if (json) {
            writeJSONImpressions();
        }
        else {

        }

    }

    public void writeJSONImpressions() {

        final JSONObject obj = (JSONObject) JSONValue.parse(this.impression);

        boolean write = false;
        FileWriter fw = null;
        try {

            write = Boolean.parseBoolean(((JSONObject) obj.get("item")).get("recommendable").toString());
        }
        catch (Exception e) {
            //
        }

        if (write) {
            try {
                fw = new FileWriter(this.fileName, true);
                fw.append(((JSONObject) obj.get("client")).get("id").toString() + ","
                                + ((JSONObject) obj.get("item")).get("id").toString() + "," + this.rating + ","
                                + Calendar.getInstance().getTimeInMillis());
                fw.append(System.getProperty("line.separator"));
            }
            catch (Exception e) {
                logger.error(e.getMessage());
            }
            finally {
                if (fw != null) {
                    try {
                        fw.close();
                    }
                    catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                }
            }

        }
    }

    public void writeStringImpressions() {

        FileWriter fw = null;

        try {
            fw = new FileWriter(this.fileName, true);
            fw.append(this.impression + "," + this.rating + "," + Calendar.getInstance().getTimeInMillis());
            fw.append(System.getProperty("line.separator"));
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
        finally {
            if (fw != null) {
                try {
                    fw.close();
                }
                catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }

        }
    }

    public void run() {
        writeImpressions();

    }
}
