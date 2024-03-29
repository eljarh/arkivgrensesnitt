package no.bouvet.sesam.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.gecko.ncore.client.core.NCore;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

public class NCoreClient {
    private static Logger log = LoggerFactory.getLogger(NCoreClient.class.getName());
    
    public void insert(DataObjectT obj) {
        insert(new DataObjectT[] { obj });
    }

    public void insert(Collection<DataObjectT> objs) {
        insert(objs.toArray(new DataObjectT[0]));
    }
  
    public void insert(DataObjectT[] objs) {
        log.info("Inserting objects {}", objs);

        try {
            NCore.Objects.insert(objs);
        } catch (Exception e) {
            String msg = "Couldn't insert fragment, ePhorte threw exception: " + e;
            log.error(msg, e);
            throw new InvalidFragment(msg, e);
        }
    }

    public void update(DataObjectT obj) {
        update(new DataObjectT[] { obj });
    }

    public void update(DataObjectT[] objs) {
        log.info("Updating objects {}", objs);

        try {
            NCore.Objects.update(objs);
        } catch (Exception e) {
            String msg = "Couldn't update fragment, ePhorte threw exception: " + e;
            log.error(msg, e);
            throw new InvalidFragment(msg, e);
        }
    }

    public List<DataObjectT> get(String searchName, String query) {
        log.debug("Getting objects using searchName {} and query {}", searchName, query);
        try {
            List<DataObjectT> objs = NCore.Objects.filteredQuery(searchName, query, new String[] {}, null, null);
            for (DataObjectT obj : objs) {
                // Remove the serialisation id since that causes
                // problems if we try to update later
                obj.setSerId(null);
            }
            return objs;
        } catch (com.sun.xml.ws.fault.ServerSOAPFaultException e) {
            log.error("Couldn't get object, ePhorte threw exception", e);
            return new ArrayList<DataObjectT>();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't do ePhorte query", e);
        }
    }

    public String upload(String fileName, String storageId, byte[] data) {
        log.info("Uploading file {} with storageId {}", fileName, storageId);

        try {
            return NCore.Documents.uploadFile(fileName, storageId, data);
        } catch (Exception e) {
            String msg = "Couldn't upload file, ePhorte threw exception: " + e;
            log.error(msg, e);
            throw new InvalidFragment(msg, e);
        }
    }
}
