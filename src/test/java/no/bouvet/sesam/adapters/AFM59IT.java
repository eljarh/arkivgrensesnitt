package no.bouvet.sesam.adapters;

import java.util.Set;

import no.gecko.ncore.client.core.NCore;

import org.apache.commons.io.IOUtils;

import org.junit.Test;

import org.junit.Before;

public class AFM59IT {
    EphorteFacade facade = EphorteFacade.getInstance();

    @Before
    public void setUp() throws Exception {
        String configData = IOUtils.toString(Utils.getResource("config.xml"));
        NCore.init(configData);
    }

    @Test
    public void testSaveBatchWithDocumentDescription() throws Exception {
        String source = Utils.getResourceAsString("afm59-save-classification.nt");
        Set<String> resourceId = Utils.getAllSubjects(source);
        BatchFragment batch = new BatchFragment(resourceId, source);
        facade.save(batch);
    }
}
