package no.bouvet.sesam.adapters;

import org.junit.Test;
import static org.junit.Assert.*;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;

public class EphorteFacadeTest {
    @Test
    public void testThatSearchStringIsCorrect(){
        String s = EphorteFacade.getSearchString("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT", "test");
        assertEquals("CustomAttribute2=test", s);
    }

    @Test
    public void testThatSearchTypeIsCorrect(){
        String s = EphorteFacade.getSearchName("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT.CaseT");
        assertEquals("Case", s);
    }
}
