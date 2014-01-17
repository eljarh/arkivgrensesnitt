package no.bouvet.sesam.adapters;

import org.junit.Test;
import static org.junit.Assert.*;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import org.junit.Before;

public class EphorteFacadeTest {
    EphorteFacade realFacade = EphorteFacade.getInstance();
    EphorteFacade mockFacade;

    static String caseProperty = "http://data.mattilsynet.org/ontology/CaseT";
    static String fqCaseT = "no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT";
    static DatatypeFactory dt;


    static {
        try {
            dt = DatatypeFactory.newInstance();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Before
    public void setUp() {
        mockFacade = mock(EphorteFacade.class);
    }

    @Test
    public void testThatFacadeCanCreateCaseT() throws Exception {
        CaseT myCase = (CaseT) realFacade.create(fqCaseT, "whatever");
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
    public void testThatGetReturnsNullOnNotFound() throws Exception {
        ArrayList<DataObjectT> result = new ArrayList<DataObjectT>();

        when(mockFacade.actualGet("Case", "CustomAttribute2=id")).thenReturn(result);
        when(mockFacade.get(fqCaseT, "id")).thenCallRealMethod();

        CaseT actual = (CaseT) mockFacade.get(fqCaseT, "id");

        assertNull(actual);
    }

    @Test
    public void testThatGetReturnsTheNewestCreatedOnManyResults() throws Exception {
        ArrayList<DataObjectT> result = new ArrayList<DataObjectT>();

        CaseT first = new CaseT();
        first.setCreatedDate(dt.newXMLGregorianCalendar("1903-03-01T00:00:00Z"));
        result.add(first);

        CaseT second = new CaseT();
        second.setCreatedDate(dt.newXMLGregorianCalendar("1993-03-01T00:00:00Z"));
        result.add(second);

        CaseT third = new CaseT();
        third.setCreatedDate(dt.newXMLGregorianCalendar("1943-03-01T00:00:00Z"));
        result.add(third);

        when(mockFacade.actualGet("Case", "CustomAttribute2=id")).thenReturn(result);
        when(mockFacade.get(fqCaseT, "id")).thenCallRealMethod();

        CaseT actual = (CaseT) mockFacade.get(fqCaseT, "id");

        assertSame(second, actual);
    }

    @Test
    public void testThatGetHandlesObjectsWithNullCreatedDate() throws Exception {
        ArrayList<DataObjectT> result = new ArrayList<DataObjectT>();

        CaseT first = new CaseT();
        result.add(first);

        CaseT second = new CaseT();
        result.add(second);

        CaseT third = new CaseT();
        result.add(third);

        when(mockFacade.actualGet("Case", "CustomAttribute2=id")).thenReturn(result);
        when(mockFacade.get(fqCaseT, "id")).thenCallRealMethod();

        CaseT actual = (CaseT) mockFacade.get(fqCaseT, "id");
        assertSame(third, actual);
    }

    @Test
    public void testThatGetHandlesObjectsWithoutCreatedDate() throws Exception {
        ArrayList<DataObjectT> result = new ArrayList<DataObjectT>();

        DataObjectT first = new DataObjectT();
        result.add(first);

        DataObjectT second = new DataObjectT();
        result.add(second);

        DataObjectT third = new DataObjectT();
        result.add(third);

        when(mockFacade.actualGet("Case", "CustomAttribute2=id")).thenReturn(result);
        when(mockFacade.get(fqCaseT, "id")).thenCallRealMethod();

        DataObjectT actual = (DataObjectT) mockFacade.get(fqCaseT, "id");
        assertSame(third, actual);
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
