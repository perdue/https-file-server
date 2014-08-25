package org.perdue.fileserver;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class HttpsFileServer {
    private static final Logger LOG = Log.getLogger(HttpsFileServer.class.toString());

    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_HTTPS_PORT = 8443;

    public static void main(String[] args) throws Exception
    {
        final String resourceBase = getResourceBase(args);
        final int ports[] = getPorts(args);
        final int httpPort = ports[0];
        final int httpsPort = ports[1];

        Config conf = ConfigFactory.load().getConfig("server");

        // ***** HTTP/HTTPS server *****
        // Create a basic jetty server object without declaring the port.
        // Since we are configuring connectors directly we'll be setting ports
        // on those connectors.
        Server server = new Server();

        // ***** HTTP/HTTPS configuration *****
        // HttpConfiguration is a collection of configuration information
        // appropriate for http and https. The default scheme for http is
        // <code>http</code> of course, as the default for secured http is
        // <code>https</code> but we show setting the scheme to show it can be
        // done. The port for secured communication is also set here.
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(httpsPort);
        httpConfig.setOutputBufferSize(32768);

        // ***** HTTP connector *****
        // The first server connector we create is the one for http, passing in
        // the http configuration we configured above so it can get things like
        // the output buffer size, etc. We also set the port and configure an
        // idle timeout.
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfig);
        ServerConnector httpConnector = new ServerConnector(server, httpConnectionFactory);
        httpConnector.setPort(httpPort);
        httpConnector.setIdleTimeout(30000);

        // ***** SSL Context Factory for HTTPS *****
        // SSL requires a certificate so we configure a factory for ssl
        // contents with information pointing to what key store the ssl
        // connection needs to know about. Much more configuration is
        // available the ssl context, including things like choosing the
        // particular certificate out of a key store to be used.
        SslContextFactory sslContextFactory = new SslContextFactory();

        Config keyStoreConf = conf.getConfig("keystore");
        sslContextFactory.setKeyStorePath(keyStoreConf.getString("keystore"));
        sslContextFactory.setKeyStorePassword(keyStoreConf.getString("keystore-pass"));

        sslContextFactory.setNeedClientAuth(true);

        Config trustStoreConf = conf.getConfig("truststore");
        sslContextFactory.setTrustStorePath(trustStoreConf.getString("truststore"));
        sslContextFactory.setTrustStorePassword(trustStoreConf.getString("truststore-pass"));

        // ***** HTTPS Configuration *****
        // A new HttpConfiguration object is needed for the next connector and
        // you can pass the old one as an argument to effectively clone the
        // contents. On this HttpConfiguration object we add a
        // SecureRequestCustomizer which is how a new connector is able to
        // resolve the HTTPS connection before handing control over to the
        // Jetty server.
        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        // ***** HTTPS connector *****
        // We create a second ServerConnector, passing in the HTTP
        // configuration along with the SSL context factory. Next we set the
        // port and a longer idle timeout.
        HttpConnectionFactory httpsConnectionFactory = new HttpConnectionFactory(httpsConfig);
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, "http/1.1");
        ServerConnector httpsConnector = new ServerConnector(server, sslConnectionFactory, httpsConnectionFactory);
        httpsConnector.setPort(httpsPort);
        httpsConnector.setIdleTimeout(500000);

        // ***** Resource handler *****
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setResourceBase(resourceBase);
        LOG.info("Serving " + resourceHandler.getBaseResource());

        // ***** Lists *****
        Connector[] connectors = new Connector[] {httpConnector, httpsConnector};
        HandlerList handlerList = new HandlerList();
        Handler[] handlers = new Handler[] {resourceHandler, new DefaultHandler()};
        handlerList.setHandlers(handlers);

        server.setConnectors(connectors);
        server.setHandler(handlerList);
        server.start();
        server.join();
    }

    private static String getResourceBase(final String[] args) {
        String base = "";
        try {
            base = args[0];
        } catch (Exception e) {
            System.out.println("\nPass in the server base directory using first argument.");
            System.exit(-1);
        }
        return base;
    }

    private static int[] getPorts(final String[] args) {
        int[] ports = new int[2];
        if(args.length == 3) {
            try {
                ports[0] = Integer.parseInt(args[1]);
                ports[1] = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.out.println("\nError parsing port numbers as an integers.");
                System.exit(-1);
            }
        }
        else {
            ports[0] = DEFAULT_HTTP_PORT;
            ports[1] = DEFAULT_HTTPS_PORT;
        }
        return ports;
    }
}
