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

public class FragmentTest {
    @Test
    public void testThatFragmentCanParseSimpleCase() throws Exception {
        String resourceId = "http://data.mattilsynet.org/cases/776663918";
        String type = "no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT";

        String source = IntegrationTest.getResourceAsString("simplecase.nt");
        Fragment fragment = new Fragment(resourceId, source);

        assertEquals(resourceId, fragment.getResourceId());
        assertEquals(type, fragment.getType());
        assertEquals(source, fragment.getSource());
        assertEquals(3, fragment.getStatements().size());
    }

    @Test
    public void testThatFragmentCanParseSimpleJournalPost() throws Exception {
        String resourceId = "http://data.mattilsynet.no/sesam/webcruiter/journalpost/974bfef7-1f72-4ee5-97ff-ffc07c8000fb";
        String type = "no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.RegistryEntryT";

        String source = IntegrationTest.getResourceAsString("simplejournalpost.nt");
        Fragment fragment = new Fragment(resourceId, source);

        assertEquals(resourceId, fragment.getResourceId());
        assertEquals(type, fragment.getType());
        assertEquals(source, fragment.getSource());
        assertEquals(6, fragment.getStatements().size());
    }
}
