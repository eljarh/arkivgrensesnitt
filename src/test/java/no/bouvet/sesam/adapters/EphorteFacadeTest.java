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
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.RegistryEntryT;
import org.mockito.Mockito;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.apache.commons.lang.builder.EqualsBuilder;
import java.util.List;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.AccessGroupT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.AccessCodeT;

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

    @Rule
    public ExpectedException exception = ExpectedException.none();

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
    public void testPopulateNonExistingFieldIsNoop() throws Exception {
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/not-existing-field", "_", true);
        CaseT actual = new CaseT();
        CaseT expected = new CaseT();

        when(mockFacade.populate(any(DataObjectT.class), any(Statement.class))).thenCallRealMethod();

        mockFacade.populate(actual, s);

        assertTrue(EqualsBuilder.reflectionEquals(expected, actual));
    }

    @Test
    public void testPopulateWithLiteral() throws Exception {
        String expectedValue = "whatever";
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/custom-attribute-1", expectedValue, true);
        CaseT obj = new CaseT();

        when(mockFacade.populate(any(DataObjectT.class), any(Statement.class))).thenCallRealMethod();

        mockFacade.populate(obj, s);

        assertEquals(expectedValue, obj.getCustomAttribute1());
    }

    @Test
    public void testPopulateWithReferencedNonEphorteType() throws Exception {
        String expectedValue = "whatever";
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/custom-attribute-1", expectedValue, false);
        CaseT obj = new CaseT();

        when(mockFacade.populate(any(DataObjectT.class), any(Statement.class))).thenCallRealMethod();

        mockFacade.populate(obj, s);

        assertEquals(expectedValue, obj.getCustomAttribute1());
    }

    @Test
    public void testPopulateWithReferencedEphorteType() throws Exception {
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/case", "id", false);
        RegistryEntryT entry = new RegistryEntryT();
        CaseT expected = new CaseT();

        when(mockFacade.get(anyString(), eq("id"))).thenReturn(expected);
        when(mockFacade.populate(any(DataObjectT.class), any(Statement.class))).thenCallRealMethod();

        DataObjectT result = mockFacade.populate(entry, s);

        assertSame(expected, entry.getCase());
        assertSame(expected, result);
    }

    @Test
    public void testPopulateWithNonExistingReferencedEphorteTypeThrows() throws Exception {
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/case", "id", false);
        RegistryEntryT entry = new RegistryEntryT();
        CaseT expected = new CaseT();

        when(mockFacade.get(anyString(), eq("id"))).thenReturn(null);
        when(mockFacade.populate(any(DataObjectT.class), any(Statement.class))).thenCallRealMethod();

        exception.expect(ReferenceNotFound.class);
        exception.expectMessage("Fragment refers to non-existing object: id");
        mockFacade.populate (entry, s);
    }

    @Test
    public void testPopulateWithManyLiterals() throws Exception {
        Statement s1 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/id", "123", false);
        Statement s2 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/title", "whatever", false);
        List<Statement> statements = new ArrayList<Statement>();
        statements.add(s1);
        statements.add(s2);

        CaseT obj = new CaseT();
        realFacade.populate(obj, statements);

        assertEquals(123, (int) obj.getId());
        assertEquals("whatever", obj.getTitle());
    }

    @Test
    public void testPopulateWithManyReferencesReturnsAllInvolvedDataObjects() throws Exception {
        Statement s1 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/access-code", "code", false);
        Statement s2 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/access-group", "group", false);
        List<Statement> statements = new ArrayList<Statement>();
        statements.add(s1);
        statements.add(s2);

        AccessCodeT code = new AccessCodeT();
        AccessGroupT group = new AccessGroupT();
        CaseT obj = new CaseT();
        when(mockFacade.get(anyString(), eq("code"))).thenReturn(code);
        when(mockFacade.get(anyString(), eq("group"))).thenReturn(group);
        when(mockFacade.populate(any(DataObjectT.class), any(Statement.class))).thenCallRealMethod();
        when(mockFacade.populate(any(DataObjectT.class), anyList())).thenCallRealMethod();

        DataObjectT[] objs = mockFacade.populate(obj, statements);

        assertEquals(3, objs.length);
    }

    @Test
    public void testThatSaveCreatesIfSubjectNotExists() throws Exception {
        String source = Utils.getResourceAsString("simplecase.nt");
        String fragmentId = Utils.getFirstSubject(source);
        Fragment fragment = new Fragment(fragmentId, source);

        EphorteFacade facade = spy(realFacade);
        doReturn(null).when(facade).get(anyString(), eq(fragmentId));
        doNothing().when(facade).insert(any(DataObjectT[].class));

        facade.save(fragment);

        verify(facade).insert(any(DataObjectT[].class));
    }


    @Test
    public void testThatSaveUpdatesIfSubjectExists() throws Exception {
        String source = Utils.getResourceAsString("simplecase.nt");
        String fragmentId = Utils.getFirstSubject(source);
        Fragment fragment = new Fragment(fragmentId, source);

        CaseT existing = new CaseT();

        EphorteFacade facade = spy(realFacade);
        doReturn(existing).when(facade).get(anyString(), eq(fragmentId));
        doNothing().when(facade).update(any(DataObjectT[].class));

        facade.save(fragment);

        verify(facade).update(any(DataObjectT[].class));
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
