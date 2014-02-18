package no.bouvet.sesam.adapters;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Test;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.ClassificationT;

public class UnpackClassificationDecoratorTest {
    @Test
    public void testThatProcessFetchesUrlAndUploadsWithFacade() throws Exception {
        EphorteFacade facade = mock(EphorteFacade.class);
        BatchFragment batch = mock(BatchFragment.class);
        Statement statement = new Statement("_", "_", "classification-system-id=ARKN\u00D8KKEL::class-id=212::description=Tilsetting", false);

        UnpackClassificationDecorator decorator = new UnpackClassificationDecorator();

        ClassificationT obj = (ClassificationT) decorator.process(facade, batch, statement);
        assertEquals(obj.getClassificationSystemId(), "ARKN\u00D8KKEL");
        assertEquals(obj.getClassId(), "212");
        assertEquals(obj.getDescription(), "Tilsetting");
    }
}
