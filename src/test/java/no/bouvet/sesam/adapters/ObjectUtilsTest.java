package no.bouvet.sesam.adapters;

import org.junit.Test;
import static org.junit.Assert.*;

import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;

public class ObjectUtilsTest {
    @Test
    public void testThatGetFieldTypeReturnsNullOnMissingField() {
        assertEquals(null, ObjectUtils.getFieldType(this, "i-dont-exist"));
    }

    @Test
    public void testGetFieldType() {
        CaseT c = new CaseT();
        String t = ObjectUtils.getFieldType(c, "series");
        assertEquals("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.SeriesT", t);
    }
}
