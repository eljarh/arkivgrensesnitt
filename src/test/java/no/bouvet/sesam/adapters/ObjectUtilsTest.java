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
  
    public class Counter {
        Integer count = 0;
        public void setCount(Integer x) {
            count = x;
        }
        public Integer getCount() {
            return count;
        }
    }

    @Test
    public void testCanSetIntegerField() {
        Counter c = new Counter();

        ObjectUtils.setFieldValue(c, "count", "13");
        assertEquals(13, (int) c.getCount());
    }

    public class Switch {
        Boolean state = false;
        public void setState(Boolean x) {
            state = x;
        }
        public Boolean getState() {
            return state;
        }
    }
  
    @Test
    public void testCanSetBooleanField() {
        Switch s = new Switch();

        ObjectUtils.setFieldValue(s, "state", "true");
        assertTrue(s.getState());

        ObjectUtils.setFieldValue(s, "state", "false");
        assertFalse(s.getState());

        ObjectUtils.setFieldValue(s, "state", "1");
        assertTrue(s.getState());
    }
}
