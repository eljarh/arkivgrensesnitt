package no.bouvet.sesam.adapters;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Test;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.ClassificationT;
import java.util.HashMap;

public class PrimaryClassificationHookTest {
    @Test
    public void testThatUnpackAndPopulate() throws Exception {
        EphorteFacade facade = mock(EphorteFacade.class);
        NCoreClient client = mock(NCoreClient.class);
        when(facade.getClient()).thenReturn(client);

        String property = "http://data.mattilsynet.no/sesam/ephorte/primary-classification";
        Statement statement = new Statement("_", property, "classification-system-id=ARKN\u00D8KKEL::class-id=212::description=Tilsetting", false);

        PrimaryClassificationHook hook = new PrimaryClassificationHook();
        hook.setFacade(facade);

        Fragment fragment = new Fragment("_");
        fragment.addStatement(statement);
        fragment.setDataObject(new CaseT());

        hook.run(fragment, new HashMap<String, Object>());
        
        verify(facade).getClient();
        verify(client).insert(any(ClassificationT.class));
    }

    @Test
    public void testCreateClassification() throws Exception {
        String value = "classification-system-id=ARKN\u00D8KKEL::class-id=212::description=Tilsetting";
        Statement s = new Statement("_", "_", value, true);

        PrimaryClassificationHook hook = new PrimaryClassificationHook();
        ClassificationT ct = hook.createClassification(s);

        assertEquals("212", ct.getClassId());
        assertEquals("ARKN\u00D8KKEL", ct.getClassificationSystemId());
        assertEquals("Tilsetting", ct.getDescription());
    }

}
