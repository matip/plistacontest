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

        // load the team properties
        try {
            properties.load(new FileInputStream(args[0]));
        }
        catch (IOException e) {
            logger.error(e.toString());
        }
        catch (Exception e) {
            logger.error(e.toString());
        }

        ContestRecommender recommender = null;

        try {
            final Class<?> transformClass = Class.forName(args[1]);
            recommender = (ContestRecommender) transformClass.newInstance();
        }
        catch (Exception e) {
            logger.error(e.toString());
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
