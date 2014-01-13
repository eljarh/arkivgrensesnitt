package no.bouvet.sesam.adapters;

import javax.ws.rs.ext.Provider;

import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import no.gecko.ncore.client.core.NCore;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import java.io.IOException;

@Provider
public class ServletContextClass implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.xml");
        try {
            if (is == null) throw new IOException("Could not get resource: config.xml");

            String configData = IOUtils.toString(is);
            NCore.init(configData);
            System.out.println("##### Configured NCore");
        } catch (IOException e) {
            System.out.println("##### Could not read configuration!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {}
}
