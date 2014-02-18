package no.bouvet.sesam.adapters;

import static org.mockito.Mockito.*;
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
import java.util.Set;

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
        BatchFragment batch = new BatchFragment(fragmentId, source);
        facade.save(batch);
    }

    @Test
    public void testSaveSimpleJournalPost() throws Exception {
        String source = Utils.getResourceAsString("simplejournalpost.nt");
        String resourceId = Utils.getFirstSubject(source);
        BatchFragment batch = new BatchFragment(resourceId, source);
        facade.save(batch);
    }

    @Test
    public void testSaveSimpleJournalPostWithManyCases() throws Exception {
        String source = Utils.getResourceAsString("simplejournalpost-many-cases.nt");
        String resourceId = Utils.getFirstSubject(source);
        BatchFragment batch = new BatchFragment(resourceId, source);
        facade.save(batch);
    }

    @Test
    public void testSaveBatchWithDocumentDescription() throws Exception {
        String source = Utils.getResourceAsString("batch-with-document-description.nt");
        Set<String> resourceId = Utils.getAllSubjects(source);
        BatchFragment batch = new BatchFragment(resourceId, source);
        facade.save(batch);
    }

    @Test
    public void testSaveSimpleDocumentDescription() throws Exception {
        String source = Utils.getResourceAsString("simpledocumentdescription.nt");
        String resourceId = Utils.getFirstSubject(source);
        BatchFragment batch = new BatchFragment(resourceId, source);
        facade.save(batch);
    }

    @Test
    public void testThatWeCanRetrieveCaseByExternalId() throws Exception {
        DataObjectT result = facade.get("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT", "http://data.mattilsynet.org/cases/776663918");
        assertNotNull(result);
    }

    @Test
    public void testThatWeCanRetrieveDocumentDescriptionByExternalId() throws Exception {
        DataObjectT result = facade.get("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.DocumentDescriptionT", "http://data.mattilsynet.no/sesam/webcruiter/dokument/dcffcd30-a4b7-4e28-bc37-f44db814bc67/desc");
        assertNotNull(result);
    }

    @Test
    public void testThatWeCanRetrieveCaseByEphorteId() throws Exception {
        DataObjectT result = facade.get("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT", "http://data.mattilsynet.no/sesam/ephorte/sak/10000260");
        assertNotNull(result);
    }

    @Test
    public void testThatWeCanUploadFile() throws Exception {
        EphorteFileDecorator decorator = new EphorteFileDecorator();
        BatchFragment batch = mock(BatchFragment.class);
        Statement s = new Statement("_", "_", "http://www.jtricks.com/download-unknown", true);
        String result = (String) decorator.process(facade, batch, s);
        assertTrue(result.startsWith("UPLOAD_{ObjectModelService}_"));
        assertTrue(result.endsWith("\\content.txt"));
    }
}
