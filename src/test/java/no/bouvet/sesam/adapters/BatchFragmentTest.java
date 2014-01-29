package no.bouvet.sesam.adapters;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class BatchFragmentTest {
    @Test
    public void testThatWeCanParseBatchWithOnlyOneFragment() throws Exception {
        String source = Utils.getResourceAsString("simplecase.nt");
        String resourceId = Utils.getFirstSubject(source);
        String type = "http://data.mattilsynet.org/ontology/ePhorte/CaseT";

        List<String> resources = new ArrayList<String>();
        resources.add(resourceId);
        BatchFragment fragments = new BatchFragment(resources, source);

        List<Fragment> result = fragments.getFragments();
        assertEquals(1, result.size());
        Fragment fragment = result.get(0);
        
        assertEquals(resourceId, fragment.getResourceId());
        assertEquals(type, fragment.getType());
        assertEquals(source, fragment.getSource());
        assertEquals(3, fragment.getStatements().size());

    }
}
