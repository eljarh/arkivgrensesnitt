package no.bouvet.sesam.adapters;

import static org.junit.Assert.*;
import org.junit.Test;

import no.priv.garshol.duke.utils.NTriplesParser;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import no.gecko.ncore.client.core.NCore;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import java.io.Reader;
import java.io.InputStreamReader;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.RegistryEntryT;

public class EphorteHandlerTest {
    @Before
    public void setUp() throws Exception {
        String configData = IOUtils.toString(IntegrationTest.getResource("config.xml"));
        NCore.init(configData);
    }

    @Test
    public void testThatEphorteHandlerCanCreateCaseT() throws Exception {
        Reader reader = new InputStreamReader(IntegrationTest.getResource("simplecase.nt"));
        EphorteHandler handler = new EphorteHandler("http://data.mattilsynet.org/cases/776663918");
        NTriplesParser.parse(reader, handler);

        CaseT myCase = (CaseT) handler.getDataObjects()[0];
    }

    @Test
    public void testThatEphorteHandlerCanLookupCaseT() throws Exception {
        Reader reader = new InputStreamReader(IntegrationTest.getResource("simplejournalpost.nt"));
        EphorteHandler handler = new EphorteHandler("http://data.mattilsynet.no/sesam/webcruiter/journalpost/974bfef7-1f72-4ee5-97ff-ffc07c8000fb");
        NTriplesParser.parse(reader, handler);

        RegistryEntryT myCase = (RegistryEntryT) handler.getDataObjects()[0];
    }
}
