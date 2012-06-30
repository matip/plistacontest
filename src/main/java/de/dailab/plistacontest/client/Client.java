package de.dailab.plistacontest.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Client {

    private final static Logger logger = LoggerFactory.getLogger(Client.class);

    public Client() {
        super();
    }

    public static void main(String[] args)
                    throws Exception {

        
        if (args.length > 1 && args[1] != null) {
            PropertyConfigurator.configure(args[1]);
        } else {
            PropertyConfigurator.configure("log4j.properties");
        }
        
        final Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(args[0]));
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }

        final Server server = new Server(Integer.parseInt(properties.getProperty("plista.port", "8080")));
        server.setHandler(new ContestHandler(properties));

        server.start();
        server.join();
    }

}
