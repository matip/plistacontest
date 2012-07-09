package de.dailab.plistacontest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dailab.plistacontest.recommender.ContestItem;
import de.dailab.plistacontest.recommender.ContestRecommender;

/**
 * Main class for handling the messages of the contest server.
 * 
 * @author till
 * 
 */
public class ContestHandler
                extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(ContestHandler.class);

    private ContestRecommender contestRecommender;

    private final int teamID;

    public ContestHandler(final Properties _properties, final ContestRecommender _contestRecommender) {

        try {
            // set the item ID
            this.teamID = Integer.parseInt(_properties.getProperty("plista.teamId"));
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("TEAM ID property must be set");
        }

        this.contestRecommender = _contestRecommender;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jetty.server.Handler#handle(java.lang.String, org.eclipse.jetty.server.Request,
     * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void handle(String arg0, Request _breq, HttpServletRequest _request, HttpServletResponse _response)
                    throws IOException, ServletException {

        if (_breq.getMethod().equals("POST")) {
            if (_breq.getContentLength() < 0) {

                // handles first message from the server - returns OK
                response(_response, _breq, null, false);
            }
            else {
                String responseText = null;
                final BufferedReader bufferedReader = _breq.getReader();
                // handles all other request from the server: impressions etc.
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    responseText = handleMessage(line);
                }
                bufferedReader.close();
                response(_response, _breq, responseText, true);

            }
        }
        else {
            // handles get requests.
            logger.debug("Get request from " + _breq.getRemoteAddr());
            response(_response, _breq, "Visit <h3><a href=\"http://irml.dailab.de\">CC IRML</a></h3>", true);
        }

    }

    /**
     * Method to handle incoming messages from the server.
     * 
     * @param _jsonString
     *            the incoming contest server message
     * @return the response to the contest server
     */
    private String handleMessage(final String _jsonString) {

        String response = null;
        // make string to json object
        final JSONObject jObj = (JSONObject) JSONValue.parse(_jsonString);
        
        String msg = "unknown";
        try {
            msg = jObj.get("msg").toString();
        }
        catch (NullPointerException e) {
            logger.debug(e.getMessage());
        }

        // write all data from the server to a file
        logger.info(_jsonString);

        if (msg.equals(ClientConstants.MSG_IMPRESSION)) {
            //hanlde recommendation impressions
            response = handleImpression(jObj);
          //send impression to the recommender
            this.contestRecommender.impression(_jsonString);

            if (response != null) {
                logger.info(response);
            }
        }
        else if (msg.equals(ClientConstants.MSG_FEEDBACK)) {
            handleFeedback(jObj);
        }
        else {
            // Error handling
            logger.info(jObj.toString());
            this.contestRecommender.error(jObj.toString());
        }

        return response;
    }

    /**
     * Method to handle impression messages.
     * 
     * @param _jsonObject
     *            incoming impression message
     * @return answer to the impression message
     */
    private String handleImpression(final JSONObject _jsonObject) {
        String repsonse = null;

        final String client = ((JSONObject) _jsonObject.get("client")).get("id").toString();

        // some impressions do not have an item id
        String id = "-1";
        try {
            id = ((JSONObject) _jsonObject.get("item")).get("id").toString();
        }
        catch (Exception e) {
            // ignore
        }

        final String limit = ((JSONObject) _jsonObject.get("config")).get("limit").toString();

        // if the impression is a recommendation request, compute
        // recommendations
        final boolean recommend = Boolean.parseBoolean(((JSONObject) _jsonObject.get("config")).get("recommend")
                        .toString());
        if (recommend) {
            logger.debug("\nREQUEST: " + _jsonObject);

            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{\"msg\":\"result\",\"items\":[");
            final List<ContestItem> recs = this.contestRecommender.recommend(client, id, _jsonObject.toString(), limit);

            for (final Iterator<ContestItem> iterator = recs.iterator(); iterator.hasNext();) {
                final ContestItem contestItem = iterator.next();
                stringBuilder.append("{\"id\":\"" + contestItem.getId() + "\"}");
                if (iterator.hasNext()) {
                    stringBuilder.append(",");
                }
            }

            stringBuilder.append("],\"team\":{\"id\":" + this.teamID + "},\"version\":\"1.0\"}");
            repsonse = stringBuilder.toString();
        }
        logger.debug("RESPOND: " + repsonse);
        return repsonse;
    }

    /**
     * Method to handle feedback messages.
     * 
     * @param _jsonObject
     *            incoming feedback message
     * @return answer to the feedback message
     */
    private void handleFeedback(JSONObject _jsonObject) {

        try {
            final JSONObject config = (JSONObject) _jsonObject.get("config");
            if (config != null) {
                logger.debug("! Feedback:" + _jsonObject.toString() + "!");
                this.contestRecommender.feedback(_jsonObject.toString());
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Response handler.
     * 
     * @param _response
     *            {@link HttpServletResponse} object
     * @param _breq
     *            the initial request
     * @param _text
     *            response text
     * @param _b
     *            boolean to set whether the response text should be sent
     * @throws IOException
     */
    private void response(HttpServletResponse _response, Request _breq, String _text, boolean _b)
                    throws IOException {

        _response.setContentType("text/html;charset=utf-8");
        _response.setStatus(HttpServletResponse.SC_OK);
        _breq.setHandled(true);

        if (_text != null && _b) {
            _response.getWriter().println(_text);
        }

    }

}
