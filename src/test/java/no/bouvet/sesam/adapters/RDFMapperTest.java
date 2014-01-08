package no.bouvet.sesam.adapters;

import static org.junit.Assert.*;
import org.junit.Test;
import no.priv.garshol.duke.utils.ObjectUtils;

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
    public void testGetResourceId() {
        String value = "http://data.mattilsynet.org/employees/557417101";
        String result = RDFMapper.getResourceId(value);
        assertEquals("557417101", result);
    }

}
