package no.bouvet.sesam.adapters;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;

import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import no.gecko.ncore.client.core.NCore;
import java.io.Reader;
import java.io.InputStreamReader;
import no.priv.garshol.duke.utils.NTriplesParser;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import no.priv.garshol.duke.utils.ObjectUtils;
import java.util.List;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ncore.client.core.ObjectModel;

public class IntegrationTest {
    @Before
    public void setUp() throws Exception {
        String configData = IOUtils.toString(getResource("config.xml"));
        NCore.init(configData);
    }

    @Ignore
    @Test
    public void testThatEphorteHandlerCanCreateCaseT() throws Exception {
        Reader reader = new InputStreamReader(getResource("simplecase.nt"));
        EphorteHandler handler = new EphorteHandler("http://data.mattilsynet.org/cases/776663918");
        NTriplesParser.parse(reader, handler);
        NCore.Objects.insert(handler.getDataObjects());
    }

    @Test
    public void testThatWeCanRetrieveCaseByExternalId() throws Exception {
        List<DataObjectT> result = NCore.Objects.filteredQuery("Case", "CustomAttribute2=http://data.mattilsynet.org/cases/776663918", new String[] {}, null, null);
        assertEquals(1, result.size());
    }

    @Test
    public void testThatWeCanDeleteCase() throws Exception {
        List<DataObjectT> result = NCore.Objects.filteredQuery("Case", "CustomAttribute1=776663918", new String[] {}, null, null);
    }

    public static InputStream getResource(String name) {
        return IntegrationTest.class.getClassLoader().getResourceAsStream(name);
    }
}
