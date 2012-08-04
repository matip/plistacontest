package de.dailab.plistacontest.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to start and stop the contest server.
 * 
 * @author till
 * 
 */
public class ContestServerController {

    private static final String serverURL = "http://contest.plista.com/api/api.php";

    private static Logger logger = LoggerFactory.getLogger(ContestServerController.class);

    public static void startServer(final String _apikey) {
        String jsonStartMessage = "{\"msg\":\"start\",\"apikey\":\"" + _apikey + "\",\"version\":1}";
        sendMessage(jsonStartMessage);
    }

    public static void stopServer(final String _apikey) {
        String jsonStopMessage = "{\"msg\":\"stop\",\"apikey\":\"" + _apikey + "\",\"version\":1}";
        sendMessage(jsonStopMessage);
    }

    private static void sendMessage(final String _message) {
        try {

            // Send data
            URL url = new URL(serverURL);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(_message);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }
            wr.close();
            rd.close();
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
