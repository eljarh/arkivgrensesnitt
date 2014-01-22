package no.bouvet.sesam.adapters;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.gecko.ncore.client.core.NCore;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

public class NCoreClient {
    private static Logger log = LoggerFactory.getLogger(NCoreClient.class.getName());
    
    public void insert(DataObjectT[] objs) throws Exception {
        log.info("Inserting objects {}", objs);
        NCore.Objects.insert(objs);
    }

    public void update(DataObjectT[] objs) throws Exception {
        log.info("Updating objects {}", objs);
        NCore.Objects.update(objs);
    }

    public List<DataObjectT> get(String searchName, String query) throws Exception {
        log.debug("Getting objects using searchName {} and query {}", searchName, query);
        return NCore.Objects.filteredQuery(searchName, query, new String[] {}, null, null);
    }

    public String upload(String fileName, String storageId, byte[] data) throws Exception {
        log.info("Uploading file {} with storageId {}", fileName, storageId);
        return NCore.Documents.uploadFile(fileName, storageId, data);
    }
}
