package ee.hm.dop.config;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import com.google.inject.servlet.GuiceFilter;
import ee.hm.dop.rest.filter.TransactionFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedJetty {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedJetty.class);
    public static final String REST_PREFIX = "/rest";

    private Server server;
    private volatile static EmbeddedJetty instance;

    private EmbeddedJetty() {
    }

    public static EmbeddedJetty instance() {
        if (instance == null) {
            instance = new EmbeddedJetty();
        }

        return instance;
    }

    public void start(int port) throws Exception {
        synchronized (this) {
            if (server == null) {
                logger.info("Starting jetty on port " + port);
                server = new Server(port);
                server.setHandler(createServletContextHandler());
                server.start();
            }
        }
    }

    private ServletContextHandler createServletContextHandler() {
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletContextHandler.setContextPath("/");

        addFilters(servletContextHandler);
        configureDynamicContentServlet(servletContextHandler);
        servletContextHandler.setGzipHandler(getGzipHandler());

        return servletContextHandler;
    }

    private GzipHandler getGzipHandler() {
        GzipHandler gzipHandler = new GzipHandler();
        gzipHandler.setIncludedMimeTypes("application/json");
        return gzipHandler;
    }

    private void configureDynamicContentServlet(ServletContextHandler servletContextHandler) {
        ServletHolder servlet = new ServletHolder(new ServletContainer());
        servlet.setInitParameter("javax.ws.rs.Application", DOPApplication.class.getCanonicalName());
        servletContextHandler.addServlet(servlet, REST_PREFIX + "/*");
    }

    private void addFilters(ServletContextHandler servletContextHandler) {
        // Filter to inject object into other filters. Must be above all others.
        servletContextHandler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        servletContextHandler.addFilter(TransactionFilter.class, REST_PREFIX + "/*", EnumSet.allOf(DispatcherType.class));
    }

    public void stop() throws Exception {
        synchronized (this) {
            if (server == null) {
                return;
            }

            if (isRunning()) {
                server.stop();
            }

            server.destroy();
            server = null;
        }
    }

    public boolean isRunning() {
        return server != null && server.getState() != AbstractLifeCycle.STOPPED;
    }
}