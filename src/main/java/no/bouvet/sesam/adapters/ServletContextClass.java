package no.bouvet.sesam.adapters;

import javax.ws.rs.ext.Provider;

import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import no.gecko.ncore.client.core.NCore;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slf4j.bridge.SLF4JBridgeHandler;

@Provider
public class ServletContextClass implements ServletContextListener {
    static Logger log = LoggerFactory.getLogger(ServletContextClass.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        /* Pipe legacy j.u.l to SLF4J */
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.xml");
        try {
            if (is == null) throw new IOException("Could not get resource: config.xml");

            String configData = IOUtils.toString(is);
            NCore.init(configData);
            log.info("##### Configured NCore");
        } catch (IOException e) {
            log.error("##### Could not read configuration!", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {}
}
