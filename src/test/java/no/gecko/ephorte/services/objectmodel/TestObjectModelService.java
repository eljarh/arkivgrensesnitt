package no.gecko.ephorte.services.objectmodel;

import java.util.List;
import java.util.UUID;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.RegistryEntryT;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import junit.framework.Assert;

import no.gecko.ncore.client.core.NCore;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

public class TestObjectModelService {
    @Before
    public void setUp() throws Exception {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.xml");
        String configData = IOUtils.toString(is);
        NCore.init(configData);
    }

    @Ignore
    @Test
    public void testInsert() throws Exception {
        // create a case
        CaseT ca = new CaseT();
        ca.setTitle("case : " + UUID.randomUUID().toString());

        // create a registry entry
        RegistryEntryT re = new RegistryEntryT();
        re.setTitle("registry entry : " + UUID.randomUUID().toString());
        re.setRegistryEntryTypeId("X");
        re.setCase(ca);
        re.setSeriesId("SAK");
        
        // insert the case and registry entry
        NCore.Objects.insert(ca, re);

        // check if original referencing is preserved
        Assert.assertTrue(ca.equals(re.getCase()));

        // check if original objects have been correctly updated
        Assert.assertTrue(ca.getId() != null);
        Assert.assertTrue(re.getId() != null);
    }
}
