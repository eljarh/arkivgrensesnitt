package no.bouvet.sesam.adapters;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.AccessCodeT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.AccessGroupT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.DocumentObjectT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.RegistryEntryT;

import org.apache.commons.lang.builder.EqualsBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EphorteFacadeTest {
    EphorteFacade facade;
    @Mock NCoreClient client;

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
        client =  mock(NCoreClient.class);
        facade = spy(new EphorteFacade (client));
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testThatFacadeCanCreateCaseT() throws Exception {
        CaseT myCase = (CaseT) facade.create(fqCaseT, "whatever");
        assertNotNull(myCase);
    }

    @Test
    public void testThatSearchStringIsCorrect() {
        String s = EphorteFacade.getExternalIdSearchString(fqCaseT, "test");
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

        when(client.get(anyString(), anyString())).thenReturn(result);

        CaseT actual = (CaseT) facade.get(fqCaseT, "id");

        assertNull(actual);
    }

    @Test
    public void testThatGetTriesEphorteIdAfterExternalId() throws Exception {
        ArrayList<DataObjectT> empty = new ArrayList<DataObjectT>();
        ArrayList<DataObjectT> result = new ArrayList<DataObjectT>();
        CaseT expected = new CaseT();
        result.add(expected);

        when(client.get("Case", "CustomAttribute2=http://psi.sesam.io/ePhorte/12345")).thenReturn(empty);
        when(client.get("Case", "Id=12345")).thenReturn(result);

        CaseT actual = (CaseT) facade.get(fqCaseT, "http://psi.sesam.io/ePhorte/12345");

        assertSame(expected, actual);
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

        when(client.get("Case", "CustomAttribute2=id")).thenReturn(result);

        CaseT actual = (CaseT) facade.get(fqCaseT, "id");

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

        when(client.get("Case", "CustomAttribute2=id")).thenReturn(result);

        CaseT actual = (CaseT) facade.get(fqCaseT, "id");
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

        when(client.get("Case", "CustomAttribute2=id")).thenReturn(result);

        DataObjectT actual = facade.get(fqCaseT, "id");
        assertSame(third, actual);
    }

    @Test
    public void testPopulateNonExistingFieldIsNoop() throws Exception {
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/not-existing-field", "_", true);
        CaseT actual = new CaseT();
        CaseT expected = new CaseT();

        facade.populate(actual, s);

        assertTrue(EqualsBuilder.reflectionEquals(expected, actual));
    }

    @Test
    public void testPopulateWithLiteral() throws Exception {
        String expectedValue = "whatever";
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/custom-attribute-1", expectedValue, true);
        CaseT obj = new CaseT();

        facade.populate(obj, s);

        assertEquals(expectedValue, obj.getCustomAttribute1());
    }

    @Test
    public void testPopulateWithReferencedNonEphorteType() throws Exception {
        String expectedValue = "whatever";
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/custom-attribute-1", expectedValue, false);
        CaseT obj = new CaseT();

        facade.populate(obj, s);

        assertEquals(expectedValue, obj.getCustomAttribute1());
    }

    @Test
    public void testPopulateWithReferencedEphorteType() throws Exception {
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/case", "id", false);
        RegistryEntryT entry = new RegistryEntryT();
        CaseT expected = new CaseT();

        doReturn(expected).when(facade).get(anyString(), eq("id"));

        DataObjectT result = facade.populate(entry, s);

        assertSame(expected, entry.getCase());
        assertSame(expected, result);
    }

    @Test
    public void testPopulateWithDecoratedProperty() throws Exception {
        String property = "http://data.mattilsynet.no/sesam/ephorte/file-path";
        String url = "http://www.jtricks.com/download-unknown";
        Statement s = new Statement("_", property, url, true);

        Decorator d = mock(Decorator.class);
        facade.setDecorator(property, d);
        facade.populate(new DocumentObjectT(), s);

        verify(d).process(url);
    }

    @Test
    public void testPopulateWithNonExistingReferencedEphorteTypeThrowsReferenceNotFound() throws Exception {
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/case", "id", false);
        RegistryEntryT entry = new RegistryEntryT();
        CaseT expected = new CaseT();

        doReturn(null).when(facade).get(anyString(), eq("id"));

        exception.expect(ReferenceNotFound.class);
        exception.expectMessage("Fragment tries to set property <http://data.mattilsynet.no/sesam/ephorte/case> to non-existent object <id>");
        facade.populate (entry, s);
    }

    @Test
    public void testPopulateWithManyLiterals() throws Exception {
        Statement s1 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/id", "123", false);
        Statement s2 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/title", "whatever", false);
        List<Statement> statements = new ArrayList<Statement>();
        statements.add(s1);
        statements.add(s2);

        CaseT obj = new CaseT();
        facade.populate(obj, statements);

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
        doReturn(code).when(facade).get(anyString(), eq("code"));
        doReturn(group).when(facade).get(anyString(), eq("group"));

        DataObjectT[] objs = facade.populate(obj, statements);

        assertEquals(3, objs.length);
    }

    @Test
    public void testThatSaveCreatesIfSubjectNotExists() throws Exception {
        String source = Utils.getResourceAsString("simplecase.nt");
        String fragmentId = Utils.getFirstSubject(source);
        Fragment fragment = new Fragment(fragmentId, source);

        doReturn(null).when(facade).get(anyString(), eq(fragmentId));

        facade.save(fragment);

        verify(client).insert(any(DataObjectT[].class));
    }

    @Test
    public void testThatSaveUploadsRdfSource() throws Exception {
        String source = Utils.getResourceAsString("simplecase.nt");
        String fragmentId = Utils.getFirstSubject(source);
        Fragment fragment = new Fragment(fragmentId, source);

        doReturn("actual").when(client).upload(anyString(), anyString(), any(byte[].class));

        DataObjectT[] result = facade.save(fragment);

        CaseT c = (CaseT) result[0];
        assertEquals("actual", c.getCustomAttribute3());
    }

    @Test
    public void testThatSaveUpdatesIfSubjectExists() throws Exception {
        String source = Utils.getResourceAsString("simplecase.nt");
        String fragmentId = Utils.getFirstSubject(source);
        Fragment fragment = new Fragment(fragmentId, source);

        CaseT existing = new CaseT();

        doReturn(existing).when(facade).get(anyString(), eq(fragmentId));

        facade.save(fragment);

        verify(client).update(any(DataObjectT[].class));
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
