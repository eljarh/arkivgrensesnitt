package no.bouvet.sesam.adapters;

import java.io.InputStreamReader;
import java.io.Reader;

import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.RegistryEntryT;
import no.gecko.ncore.client.core.NCore;
import no.priv.garshol.duke.utils.NTriplesParser;

import org.apache.commons.io.IOUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class FragmentTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

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
        assertEquals(7, fragment.getStatements().size());
    }

    @Test
    public void testThatUnparseableFragmentThrowsInvalidFragmentException() throws Exception {
        exception.expect(InvalidFragment.class);
        exception.expectMessage("Couldn't parse fragment");
        Fragment fragment = new Fragment("_", "this is not a valid fragment");
    }

    @Test
    public void testThatFragmentWithoutResourceIdThrowsInvalidFragmentException() throws Exception {
        exception.expect(InvalidFragment.class);
        exception.expectMessage("Fragment has no identity");
        Fragment fragment = new Fragment(null, "<id> <prop> \"this is valid\" .");
    }

    @Test
    public void testThatFragmentWithoutTypeThrowsThrowsInvalidFragmentException() throws Exception {
        exception.expect(InvalidFragment.class);
        exception.expectMessage("Fragment has no type");
        Fragment fragment = new Fragment("<id>", "<id> <prop> \"this is valid\" .");
    }
}
