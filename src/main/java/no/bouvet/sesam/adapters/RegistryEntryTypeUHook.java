
package no.bouvet.sesam.adapters;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.RegistryEntryT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.SenderRecipientT;

/**
 * This hook runs on registry entries (journal posts) which have the
 * type 'U'. ePhorte does not allow us to set the status 'J' directly
 * on these, and so we must do a silly dance before ePhorte will
 * accept them.
 *
 * <p>The dance looks like this:
 * <ol>
 *   <li>Create registry entry with Status=R, EntryType=U.
 *   <li>Create SenderRecipientT with fake data, attach to entry by ID.
 *   <li>Update registry entry with Status=J and other data.
 * </ol>
 *
 * <p>The hook takes care of the first two steps, while normal
 * processing does the third step.
 */
public class RegistryEntryTypeUHook implements Hook {
    private static Logger log = LoggerFactory.getLogger(RegistryEntryTypeUHook.class.getName());
    private EphorteFacade facade;
    private String fake_recipient_name;

    public void setFacade(EphorteFacade facade) {
        this.facade = facade;
        this.fake_recipient_name = facade.getConfigProperty("fake.recipient.name");
        if (fake_recipient_name == null)
            fake_recipient_name = "Fake recipient";
    }

    public void run(Fragment fragment, Map<String, Object> ids) {
        // --- SHOULD WE DANCE?
        // we only deal with registry entries
        DataObjectT obj = fragment.getDataObject();
        if (!(obj instanceof RegistryEntryT))
            return;

        // if the type is already set we have nothing to do
        RegistryEntryT entry = (RegistryEntryT) obj;
        if (entry.getRegistryEntryTypeId() != null)
            return;

        // we only handle entries where type is "U"
        Statement s = fragment.getStatementWithSuffix("/registry-entry-type-id");
        if (s == null || !s.object.equals("U"))
            return;

        log.debug("Running hook on {}", fragment.getResourceId());

        // --- DANCE, SISTER, DANCE
        // STEP 1: first attempt at entry
        // set initial state
        facade.populate(fragment, ids); // set state as originally defined
        entry.setRecordStatusId("R");   // override status
        
        // send initial SOAP request
        NCoreClient client = facade.getClient();
        client.insert(entry);

        // STEP 2: make sender
        SenderRecipientT sender = new SenderRecipientT();
        sender.setName("Fake WebCruiter recipient");
        sender.setRegistryEntryId(entry.getId());
        log.debug("Making sender", sender);
        client.insert(sender);

        // STEP 3: update entry
        entry.setSenderRecipient(fake_recipient_name);
        client.update(entry);

        // STEP 4: set Status=J (done by normal processing)
        log.debug("Done");
    }
}
