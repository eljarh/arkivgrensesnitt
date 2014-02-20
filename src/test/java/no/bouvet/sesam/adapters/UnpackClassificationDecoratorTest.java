package no.bouvet.sesam.adapters;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Test;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.ClassificationT;

public class UnpackClassificationDecoratorTest {
    @Test
    public void testThatUnpackAndPopulate() throws Exception {
        EphorteFacade facade = mock(EphorteFacade.class);
        BatchFragment batch = mock(BatchFragment.class);
        Statement statement = new Statement("_", "_", "classification-system-id=ARKN\u00D8KKEL::class-id=212::description=Tilsetting", false);

        UnpackClassificationDecorator decorator = new UnpackClassificationDecorator();

        CaseT c = new CaseT();
        Fragment fragment = new Fragment("_");
        fragment.setDataObject(c);
        ClassificationT obj = (ClassificationT) decorator.process(fragment, statement);
        assertEquals(obj.getClassificationSystemId(), "ARKN\u00D8KKEL");
        assertEquals(obj.getClassId(), "212");
        assertEquals(obj.getDescription(), "Tilsetting");
    }
}
