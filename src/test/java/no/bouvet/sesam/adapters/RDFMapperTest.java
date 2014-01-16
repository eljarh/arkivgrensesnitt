package no.bouvet.sesam.adapters;

import static org.junit.Assert.*;
import org.junit.Test;
import no.priv.garshol.duke.utils.ObjectUtils;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;

public class RDFMapperTest {
    @Test
    public void testGetObjectType() {
        String property = "http://data.mattilsynet.org/ontology/CaseT";
        String result = RDFMapper.getObjectType(property);
        assertEquals("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT", result);
    }

    @Test
    public void testGetFieldName() {
        String property = "http://data.mattilsynet.org/ontology/title";
        String result = RDFMapper.getFieldName(property);
        assertEquals("title", result);
    }

    @Test
    public void testGetFieldType() {
        CaseT c = new CaseT();
        String t = RDFMapper.getFieldType(c, "series");
        assertEquals("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.SeriesT", t);
    }

    @Test
    public void testGetFirstSubject() {
        String subject = RDFMapper.getFirstSubject("<id> blubber");
        assertEquals("id", subject);
    }

    @Test
    public void testIsEphorteType() {
        assertTrue(RDFMapper.isEphorteType("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.SeriesT"));
    }
}
