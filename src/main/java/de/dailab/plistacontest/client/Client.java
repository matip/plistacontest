package de.dailab.plistacontest.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dailab.plistacontest.recommender.ContestRecommender;

public class Client {

    private final static Logger logger = LoggerFactory.getLogger(Client.class);

    public Client() {
        super();
    }

    public static void main(String[] args)
            throws Exception {

        final Properties properties = new Properties();
        String fileName = "";
        String recommenderClass = args[1];
        if(System.getProperty("plista.team") != null)
            fileName = System.getProperty("plista.team");
        else
            fileName = args[0];
        // load the team properties
        try {
            properties.load(new FileInputStream(fileName));
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
        ContestRecommender recommender = null;
        recommenderClass = (properties.getProperty("plista.recommender").isEmpty() ? recommenderClass : args[1]);
        try {
            final Class<?> transformClass = Class.forName(recommenderClass);
            recommender = (ContestRecommender) transformClass.newInstance();
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            throw new IllegalArgumentException("No recommender specified or recommender not avialable.");
        }
        // configure log4j
        if (args.length >= 3 && args[2] != null) {
            PropertyConfigurator.configure(args[0]);
        }
        else {
            PropertyConfigurator.configure("log4j.properties");
        }
        // set up and start server
        final Server server = new Server(Integer.parseInt(properties.getProperty("plista.port", "8080")));
        server.setHandler(new ContestHandler(properties, recommender));
        logger.debug("Serverport " + server.getConnectors()[0].getPort());

        server.start();
        server.join();
    }

}
