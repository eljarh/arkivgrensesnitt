package no.bouvet.sesam.adapters;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Set;
import org.junit.rules.ExpectedException;
import org.junit.Rule;
import java.util.Arrays;

public class BatchFragmentTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testThatWeCanParseBatchWithOnlyOneFragment() throws Exception {
        String source = Utils.getResourceAsString("simplecase.nt");
        String resourceId = Utils.getFirstSubject(source);
        String type = "http://data.mattilsynet.org/ontology/ePhorte/CaseT";

        List<String> resources = new ArrayList<String>();
        resources.add(resourceId);
        BatchFragment batch = new BatchFragment(resources, source);

        List<Fragment> fragments = batch.getFragments();
        assertEquals(1, fragments.size());
        Fragment fragment = fragments.get(0);

        assertEquals(resourceId, fragment.getResourceId());
        assertEquals(type, fragment.getType());
        assertEquals(source, fragment.getSource());
        assertEquals(3, fragment.getStatements().size());
    }

    @Test
    public void testThatWeCanParseBatchWithTwoFragments() throws Exception {
        String source1 = Utils.getResourceAsString("simplecase.nt");
        String source2 = Utils.getResourceAsString("simplejournalpost.nt");
        String resourceId1 = Utils.getFirstSubject(source1);
        String resourceId2 = Utils.getFirstSubject(source2);
        String source = source1 + "\n" + source2;

        List<String> resources = new ArrayList<String>();
        resources.add(resourceId1);
        resources.add(resourceId2);

        BatchFragment batch = new BatchFragment(resources, source);
        List<Fragment> fragments = batch.getFragments();

        assertEquals(2, fragments.size());

        Fragment fragment1 = fragments.get(1);
        Fragment fragment2 = fragments.get(0);

        assertEquals(resourceId1, fragment1.getResourceId());
        assertEquals(source1, fragment1.getSource());
        assertEquals(resourceId2, fragment2.getResourceId());
        assertEquals(source2, fragment2.getSource());
    }

    @Test
    public void testThatBatchWithFragmentWithoutTypeThrowsInvalidFragment() throws Exception {
        exception.expect(InvalidFragment.class);
        exception.expectMessage("Fragment has no type");
        BatchFragment batch = new BatchFragment(Arrays.asList(new String[] { "<id>" }), "<id> <prop> \"this is valid\" .");
    }

    @Test
    public void testThatGetFragmentsThrowsInvalidFragmentOnCyclicGraph() throws Exception {
        String source = Utils.getResourceAsString("batch-with-cycles.nt");
        Set<String> resources = Utils.getAllSubjects(source);

        BatchFragment batch = new BatchFragment(resources, source);

        exception.expect(InvalidFragment.class);
        exception.expectMessage("Fragment contains cycles");
        List<Fragment> fragments = batch.getFragments();
    }
    @Test
    public void testThatGetFragmentsIsSortedByDependencyOrder() throws Exception {
        String source = Utils.getResourceAsString("simplebatch.nt");
        Set<String> resources = Utils.getAllSubjects(source);

        BatchFragment batch = new BatchFragment(resources, source);

        List<Fragment> fragments = batch.getFragments();
        assertEquals(5, fragments.size());

        String[] expected = new String[] { "DocumentObjectT", "DocumentDescriptionT", "CaseT", "RegistryEntryT", "RegistryEntryDocumentT" };

        for (int i = 0; i < expected.length; i++) {
            String type = fragments.get(i).getType();
            String name = Utils.getLastPart(type);
            assertEquals(expected[i], name);
        }
    }
}
