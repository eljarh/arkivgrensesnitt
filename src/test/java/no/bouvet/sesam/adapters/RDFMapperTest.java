package no.bouvet.sesam.adapters;

import static org.junit.Assert.*;
import org.junit.Test;
import no.priv.garshol.duke.utils.ObjectUtils;

public class RDFMapperTest {
    @Test
    public void lookupObjectType_ontology_Case() {
        String property = "http://data.mattilsynet.org/ontology/CaseT";
        String result = RDFMapper.lookupObjectType(property);
        assertEquals("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT", result);
    }
}
