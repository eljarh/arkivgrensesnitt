
package no.bouvet.sesam.adapters;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

import org.apache.commons.configuration.PropertiesConfiguration;

// we need some tests to make sure we handle the corner cases in
// PropertiesConfiguration correctly
public class EphorteFacadeConfigTest {

    @Test
    public void testCommalessConfig() throws Exception {
        PropertiesConfiguration cfg = load("commaless-config.properties");
        PropertiesConfiguration decorators = new PropertiesConfiguration();

        EphorteFacade eph = new EphorteFacade();
        eph.init(cfg, decorators);

        assertEquals(1, eph.getImmutableProperties().size());
        assertTrue(eph.getImmutableProperties().contains("http://data.mattilsynet.no/sesam/ephorte/primary-classification"));
    }

    @Test
    public void testCommaConfig() throws Exception {
        PropertiesConfiguration cfg = load("comma-config.properties");
        PropertiesConfiguration decorators = new PropertiesConfiguration();

        EphorteFacade eph = new EphorteFacade();
        eph.init(cfg, decorators);

        assertEquals(2, eph.getImmutableProperties().size());
        assertTrue(eph.getImmutableProperties().contains("http://data.mattilsynet.no/sesam/ephorte/primary-classification"));
        assertTrue(eph.getImmutableProperties().contains("http://data.mattilsynet.no/sesam/ephorte/registry-entry-type-id"));
    }
    
    private PropertiesConfiguration load(String resource) throws Exception {
        PropertiesConfiguration cfg = new PropertiesConfiguration();
        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        InputStream istream = cloader.getResourceAsStream(resource);
        cfg.load(istream);
        return cfg;
    }
}
