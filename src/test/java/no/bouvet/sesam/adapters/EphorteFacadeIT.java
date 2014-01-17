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
import java.util.List;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ncore.client.core.ObjectModel;

import org.apache.commons.io.IOUtils;

public class EphorteFacadeIT {
    EphorteFacade facade = EphorteFacade.getInstance();

    @Before
    public void setUp() throws Exception {
        String configData = IOUtils.toString(Utils.getResource("config.xml"));
        NCore.init(configData);
    }

    @Test
    public void testSaveSimpleCase() throws Exception {
        String source = Utils.getResourceAsString("simplecase.nt");
        String fragmentId = Utils.getFirstSubject(source);
        Fragment fragment = new Fragment(fragmentId, source);
        facade.save(fragment);
    }

    @Test
    public void testSaveSimpleJournalPost() throws Exception {
        String source = Utils.getResourceAsString("simplejournalpost.nt");
        String resourceId = Utils.getFirstSubject(source);
        Fragment fragment = new Fragment(resourceId, source);
        facade.save(fragment);
    }

    @Test
    public void testThatWeCanRetrieveCaseByExternalId() throws Exception {
        List<DataObjectT> result = NCore.Objects.filteredQuery("Case", "CustomAttribute2=http://data.mattilsynet.org/cases/776663918", new String[] {}, null, null);
        assertEquals(1, result.size());
    }
}
