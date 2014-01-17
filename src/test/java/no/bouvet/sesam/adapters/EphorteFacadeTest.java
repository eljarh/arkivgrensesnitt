package no.bouvet.sesam.adapters;

import org.junit.Test;
import static org.junit.Assert.*;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import java.util.ArrayList;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeFactory;

public class EphorteFacadeTest {
    EphorteFacade facade = EphorteFacade.getInstance();

    static String caseProperty = "http://data.mattilsynet.org/ontology/CaseT";
    static String fqCaseT = "no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT";
    static DatatypeFactory dt;
    
    static {
        try {
            dt = DatatypeFactory.newInstance();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Test
    public void testThatFacadeCanCreateCaseT() throws Exception {
        CaseT myCase = (CaseT) facade.create(fqCaseT, "whatever");
        assertNotNull(myCase);
    }

    @Test
    public void testThatSearchStringIsCorrect() {
        String s = EphorteFacade.getSearchString(fqCaseT, "test");
        assertEquals("CustomAttribute2=test", s);
    }

    @Test
    public void testThatSearchTypeIsCorrect() {
        String s = EphorteFacade.getSearchName(fqCaseT);
        assertEquals("Case", s);
    }

    @Test
    public void testGetObjectType() {
        String property = "http://data.mattilsynet.org/ontology/CaseT";
        String result = EphorteFacade.getObjectType(property);
        assertEquals("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT", result);
    }

    @Test
    public void testGetFieldName() {
        String property = "http://data.mattilsynet.org/ontology/title";
        String result = EphorteFacade.getFieldName(property);
        assertEquals("title", result);
    }

    @Test
    public void testIsEphorteType() {
        assertTrue(EphorteFacade.isEphorteType("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.SeriesT"));
    }

    @Test
    public void testThatGetObjectTypeReturnsEmptyOnNull() {
        assertEquals("", EphorteFacade.getObjectType(null));
    }

    @Test
    public void testThatGetFieldNameReturnsEmptyOnNull() {
        assertEquals("", EphorteFacade.getFieldName(null));
    }
}
