package no.bouvet.sesam.adapters;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.gecko.ncore.client.core.NCore;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import java.util.ArrayList;

public class NCoreClient {
    private static Logger log = LoggerFactory.getLogger(NCoreClient.class.getName());
    
    public void insert(DataObjectT[] objs) throws Exception {
        log.info("Inserting objects {}", objs);

        try {
            NCore.Objects.insert(objs);
        } catch (Exception e) {
            String msg = "Couldn't insert fragment, ePhorte threw exception";
            log.error(msg, e);
            throw new InvalidFragment(msg, e);
        }
    }

    public void update(DataObjectT[] objs) throws Exception {
        log.info("Updating objects {}", objs);

        try {
            NCore.Objects.update(objs);
        } catch (Exception e) {
            String msg = "Couldn't update fragment, ePhorte threw exception";
            log.error(msg, e);
            throw new InvalidFragment(msg, e);
        }
    }

    public List<DataObjectT> get(String searchName, String query) throws Exception {
        log.debug("Getting objects using searchName {} and query {}", searchName, query);
        try {
            return NCore.Objects.filteredQuery(searchName, query, new String[] {}, null, null);
        } catch (com.sun.xml.ws.fault.ServerSOAPFaultException e) {
            log.error("Couldn't get object, ePhorte threw exception", e);
            return new ArrayList<DataObjectT>();
        }
    }

    public String upload(String fileName, String storageId, byte[] data) throws Exception {
        log.info("Uploading file {} with storageId {}", fileName, storageId);

        try {
            return NCore.Documents.uploadFile(fileName, storageId, data);
        } catch (Exception e) {
            String msg = "Couldn't upload file, ePhorte threw exception";
            log.error(msg, e);
            throw new InvalidFragment(msg, e);
        }
    }
}
