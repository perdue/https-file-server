package org.perdue.fileserver;

import java.io.File;
import java.net.URL;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class Main {
    private static final Logger LOG = Log.getLogger(Main.class.toString());

    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception
    {
        final int port = getPort(args);
  
        Server server = new Server(port);
  
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
  
        resource_handler.setResourceBase(".");
        LOG.info("serving " + resource_handler.getBaseResource());

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
        server.setHandler(handlers);

        server.start();
        server.join();
    }

    private static int getPort(final String[] args) {
        if(args.length == 1) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing port number as an integer: "+ args[0]);
                System.exit(-1);
            }
        }
        return DEFAULT_PORT;
    }


}
