package no.bouvet.sesam.adapters;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import no.gecko.ncore.client.core.NCore;
import java.io.Reader;
import java.io.InputStreamReader;
import no.priv.garshol.duke.utils.NTriplesParser;

public class IntegrationTest {
    @Before
    public void setUp() throws Exception {
        String configData = IOUtils.toString(getResource("config.xml"));
        NCore.init(configData);
    }

    @Test
    public void testThatEphorteHandlerCanCreateCaseT() throws Exception {
        Reader reader = new InputStreamReader(getResource("simplecase.nt"));
        EphorteHandler handler = new EphorteHandler();
        NTriplesParser.parse(reader, handler);
        NCore.Objects.insert(handler.getDataObjects());
    }

    private InputStream getResource(String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }
}
