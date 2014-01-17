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
        String source = Utils.getResourceAsString("simplecase.nt");
        String resourceId = Utils.getFirstSubject(source);
        String type = "http://data.mattilsynet.org/ontology/ePhorte/CaseT";

        Fragment fragment = new Fragment(resourceId, source);

        assertEquals(resourceId, fragment.getResourceId());
        assertEquals(type, fragment.getType());
        assertEquals(source, fragment.getSource());
        assertEquals(3, fragment.getStatements().size());
    }

    @Test
    public void testThatFragmentCanParseSimpleJournalPost() throws Exception {
        String source = Utils.getResourceAsString("simplejournalpost.nt");
        String resourceId = Utils.getFirstSubject(source);
        String type = "http://data.mattilsynet.no/sesam/ephorte/RegistryEntryT";

        Fragment fragment = new Fragment(resourceId, source);

        assertEquals(resourceId, fragment.getResourceId());
        assertEquals(type, fragment.getType());
        assertEquals(source, fragment.getSource());
        assertEquals(6, fragment.getStatements().size());
    }
}
