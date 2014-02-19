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
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.ClassificationT;
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
import java.util.Set;

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
        facade = spy(new EphorteFacade(client));
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
        String s = facade.getExternalIdSearchString(fqCaseT, "test");
        assertEquals("CustomAttribute5=BASE32:ORSXG5A=", s);
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
        ArrayList<DataObjectT> result = new ArrayList<DataObjectT>();
        CaseT expected = new CaseT();
        result.add(expected);

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

        when(client.get(anyString(), anyString())).thenReturn(result);

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

        when(client.get(anyString(), anyString())).thenReturn(result);

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

        when(client.get(anyString(), anyString())).thenReturn(result);

        DataObjectT actual = facade.get(fqCaseT, "id");
        assertSame(third, actual);
    }

    @Test
    public void testPopulateNonExistingFieldIsNoop() throws Exception {
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/not-existing-field", "_", true);
        CaseT actual = new CaseT();
        CaseT expected = new CaseT();

        facade.populate(actual, mock(BatchFragment.class), s);

        assertTrue(EqualsBuilder.reflectionEquals(expected, actual));
    }

    @Test
    public void testPopulateWithLiteral() throws Exception {
        String expectedValue = "whatever";
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/custom-attribute-1", expectedValue, true);
        CaseT obj = new CaseT();

        facade.populate(obj, mock(BatchFragment.class), s);

        assertEquals(expectedValue, obj.getCustomAttribute1());
    }

    @Test
    public void testPopulateWithReferencedNonEphorteType() throws Exception {
        String expectedValue = "whatever";
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/custom-attribute-1", expectedValue, false);
        CaseT obj = new CaseT();

        facade.populate(obj, mock(BatchFragment.class), s);

        assertEquals(expectedValue, obj.getCustomAttribute1());
    }

    @Test
    public void testPopulateWithReferencedEphorteType() throws Exception {
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/case", "id", false);
        RegistryEntryT entry = new RegistryEntryT();
        Integer expected = 12345;
        CaseT c = new CaseT();
        c.setId(expected);

        doReturn(c).when(facade).get(anyString(), eq("id"));

        facade.populate(entry, mock(BatchFragment.class), s);

        assertEquals(expected, entry.getCaseId());
    }

    @Test
    public void testPopulateWithDecoratedProperty() throws Exception {
        String property = "http://data.mattilsynet.no/sesam/ephorte/file-path";
        String url = "http://www.jtricks.com/download-unknown";
        Statement s = new Statement("_", property, url, true);
        BatchFragment batch = mock(BatchFragment.class);

        Decorator d = mock(Decorator.class);
        facade.setDecorator(property, d);
        facade.populate(new DocumentObjectT(), batch, s);

        verify(d).process(facade, batch, s);
    }

    @Test
    public void testPopulateWithUnpackClassificationDecorator() throws Exception {
        String property = "http://data.mattilsynet.no/sesam/ephorte/primary-classification";
        String value = "classification-system-id=ARKN\u00D8KKEL::class-id=212::description=Tilsetting";

        Statement s = new Statement("_", property, value, true);
        BatchFragment batch = mock(BatchFragment.class);

        Decorator d = mock(Decorator.class);

        doReturn(new ClassificationT()).when(d).process(facade, batch, s);

        facade.setDecorator(property, d);
        facade.populate(new CaseT(), batch, s);

        verify(d).process(facade, batch, s);
    }

    @Test
    public void testPopulateWithNonExistingReferencedEphorteTypeThrowsReferenceNotFound() throws Exception {
        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/case", "missing", false);
        RegistryEntryT entry = new RegistryEntryT();
        CaseT expected = new CaseT();

        doReturn(null).when(facade).get(anyString(), eq("missing"));

        exception.expect(ReferenceNotFound.class);
        exception.expectMessage("Fragment <_> tries to set property <http://data.mattilsynet.no/sesam/ephorte/case> to non-existent object <missing>");
        facade.populate(entry, mock(BatchFragment.class), s);
    }

    @Test
    public void testPopulateWithManyLiterals() throws Exception {
        Statement s1 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/id", "123", false);
        Statement s2 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/title", "whatever", false);
        List<Statement> statements = new ArrayList<Statement>();
        statements.add(s1);
        statements.add(s2);

        BatchFragment batch = mock(BatchFragment.class);
        doReturn(statements).when(batch).getStatements(anyString());

        CaseT obj = new CaseT();
        facade.populate(obj, batch, "_");

        assertEquals(123, (int) obj.getId());
        assertEquals("whatever", obj.getTitle());
    }

    @Test
    public void testPopulateWithManyReferences_ExistingOverwritesNonExisting() throws Exception {
        Statement s1 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/access-code", "exists", false);
        Statement s2 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/access-code", "missing", false);
        List<Statement> statements = new ArrayList<Statement>();
        statements.add(s1);
        statements.add(s2);

        String codeId = "123";
        AccessCodeT code = new AccessCodeT();
        code.setId(codeId);
        CaseT obj = new CaseT();

        doReturn(code).when(facade).get(anyString(), eq("exists"));
        doReturn(null).when(facade).get(anyString(), eq("missing"));

        BatchFragment batch = mock(BatchFragment.class);
        doReturn(statements).when(batch).getStatements(anyString());

        facade.populate (obj, batch, "_");
        assertEquals(codeId, obj.getAccessCodeId());
    }

    @Test
    public void testPopulateWithManyReferences_MissingThrowsException() throws Exception {
        Statement s1 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/access-code", "missing", false);
        Statement s2 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/access-code", "missing", false);
        List<Statement> statements = new ArrayList<Statement>();
        statements.add(s1);
        statements.add(s2);

        CaseT obj = new CaseT();

        doReturn(null).when(facade).get(anyString(), eq("missing"));

        BatchFragment batch = mock(BatchFragment.class);
        doReturn(statements).when(batch).getStatements(anyString());

        exception.expect(ReferenceNotFound.class);
        exception.expectMessage("Fragment <_> tries to set property <http://data.mattilsynet.no/sesam/ephorte/access-code> to non-existent object <missing>");
        facade.populate (obj, batch, "_");
    }

    @Test
    public void testPopulateWithManyReferencesSetsIdsCorrectly() throws Exception {
        Statement s1 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/access-code", "code", false);
        Statement s2 = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/access-group", "group", false);
        List<Statement> statements = new ArrayList<Statement>();
        statements.add(s1);
        statements.add(s2);

        String codeId = "123";
        Integer groupId = 345;
        AccessCodeT code = new AccessCodeT();
        code.setId(codeId);
        AccessGroupT group = new AccessGroupT();
        group.setId(groupId);
        CaseT obj = new CaseT();
        doReturn(code).when(facade).get(anyString(), eq("code"));
        doReturn(group).when(facade).get(anyString(), eq("group"));
        BatchFragment batch = mock(BatchFragment.class);
        doReturn(statements).when(batch).getStatements(anyString());

        facade.populate(obj, batch, "_");

        assertEquals(codeId, obj.getAccessCodeId());
        assertEquals(groupId, obj.getAccessGroupId());
    }

    @Test
    public void testThatSaveCreatesIfSubjectNotExists() throws Exception {
        String source = Utils.getResourceAsString("simplecase.nt");
        String fragmentId = Utils.getFirstSubject(source);
        BatchFragment batch = new BatchFragment(fragmentId, source);

        doReturn(null).when(facade).get(anyString(), eq(fragmentId));

        facade.save(batch);

        verify(client).insert(any(new DataObjectT[0].getClass()));
    }

    @Test
    public void testThatSaveUploadsRdfSource() throws Exception {
        String source = Utils.getResourceAsString("simplecase.nt");
        String fragmentId = Utils.getFirstSubject(source);
        BatchFragment batch = new BatchFragment(fragmentId, source);

        doReturn("actual").when(client).upload(anyString(), anyString(), any(byte[].class));

        CaseT result = (CaseT) facade.save(batch)[0];
        assertEquals("actual", result.getCustomAttribute6());
    }

    @Test
    public void testThatSaveHandlesBatchFragment() throws Exception {
        String source = Utils.getResourceAsString("simplebatch.nt");
        Set<String> fragmentIds = Utils.getAllSubjects(source);
        BatchFragment batch = new BatchFragment(fragmentIds, source);

        for (Fragment f : batch.getFragments()) {
            String fragmentId = f.getResourceId();
            String ePhorteType = EphorteFacade.getObjectType(f.getType());
            DataObjectT obj = facade.create(ePhorteType, fragmentId);
            try {
                ObjectUtils.setFieldValue(obj, "id", 123);
            } catch (Exception e) { /* ok */ }
            doReturn(null).when(facade).get(anyString(), eq(fragmentId));
            doReturn(obj).when(facade).get(anyString(), eq(fragmentId));
        }

        DataObjectT[] result = facade.save(batch);

        // FIXME: This test is a bit broken.  We actually want 5
        // inserts, however to get this, we need to return null on the
        // first call to get() then the object on the second call to get
        verify(client, times(5)).update(any(new DataObjectT[0].getClass()));
    }

    @Test
    public void testThatSaveUpdatesIfSubjectExists() throws Exception {
        String source = Utils.getResourceAsString("simplecase.nt");
        String fragmentId = Utils.getFirstSubject(source);
        BatchFragment batch = new BatchFragment(fragmentId, source);

        CaseT existing = new CaseT();

        doReturn(existing).when(facade).get(anyString(), eq(fragmentId));

        facade.save(batch);

        verify(client).update(any(new DataObjectT[0].getClass()));
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

    @Test
    public void testIgnoredReference() throws Exception {
        facade.addIgnoredReferencePrefix("http://ignored/");

        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/case", "http://ignored/id", false);
        RegistryEntryT entry = new RegistryEntryT();
        Integer expected = 12345;
        CaseT thecase = new CaseT();
        thecase.setId(expected);

        doReturn(thecase).when(facade).get(anyString(), eq("http://ignored/id"));

        facade.populate(entry, mock(BatchFragment.class), s);
        assertSame(null, entry.getCaseId());
    }

    @Test
    public void testImmutableProperty() throws Exception {
        String property = "http://data.mattilsynet.no/sesam/ephorte/primary-classification";
        facade.addImmutableProperty(property);

        Statement s = new Statement("_", property, "_", false);
        CaseT thecase = new CaseT();
        ClassificationT val = new ClassificationT();
        thecase.setPrimaryClassification(val);

        facade.populate(thecase, mock(BatchFragment.class), s);
        assertSame(val, thecase.getPrimaryClassification());
    }

    @Test
    public void testUnignoredReference() throws Exception {
        facade.addIgnoredReferencePrefix("http://ignored/");

        Statement s = new Statement("_", "http://data.mattilsynet.no/sesam/ephorte/case", "http://unignored/id", false);
        RegistryEntryT entry = new RegistryEntryT();
        CaseT thecase = new CaseT();
        Integer expected = 12345;
        thecase.setId(expected);

        doReturn(thecase).when(facade).get(anyString(), eq("http://unignored/id"));

        facade.populate(entry, mock(BatchFragment.class), s);

        assertEquals(expected, entry.getCaseId());
    }
}
