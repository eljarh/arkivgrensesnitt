
package no.bouvet.sesam.adapters;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.RegistryEntryT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.SenderRecipientT;

/**
 * This post-hook is the final piece of the fix to avoid getting
 * validation errors with registry entries with type "U" and status
 * "J".  These cannot go into status "J" before they have
 * sender/recipient, but there may or may not be one. In the case
 * where there is none, a hook creates a fake sender/recipient and all
 * is well.
 *
 * <p>This hook is for when there is a real SenderRecipientT. In that
 * case, the hook sets the status to "R". Later the SenderRecipientT
 * arrives and is created, and afterwards this hook runs, and sets the
 * status of the registry entry to the correct value, which we can
 * find in the eph:registry-entry-status property. This solves AFM-94.
 */
public class SenderRecipientHook implements Hook {
    private static Logger log = LoggerFactory.getLogger(SenderRecipientHook.class.getName());
    private EphorteFacade facade;
    private NCoreClient client;

    public void setFacade(EphorteFacade facade) {
        this.facade = facade;
        this.client = facade.getClient();
    }

    public void run(Fragment fragment, Map<String, Object> ids) {
        DataObjectT obj = fragment.getDataObject();
        if (!(obj instanceof SenderRecipientT))
            return; // nothing to do

        SenderRecipientT sr = (SenderRecipientT) obj;
        Integer re_id = sr.getRegistryEntryId();
        log.trace("Running on SenderRecipientT.id={}, on RegistryEntryT.id=",
                  sr.getId(), re_id);

        Statement s = fragment.getStatementWithSuffix("/registry-entry-status");
        if (s == null) {
            log.trace("No registry-entry-status found");
            return;
        }

        RegistryEntryT re = (RegistryEntryT) 
            facade.get("RegistryEntry", "Id=" + re_id);
        re.setRecordStatusId(s.object);
        log.trace("Set record status id to {}", s.object);
        client.update(re);
    }
}
