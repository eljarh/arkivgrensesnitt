
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
 *   <li>Create registry the normal way, override Status=R.
 *   <li>Create SenderRecipientT with fake data, attach to entry by ID.
 *   <li>Update registry entry the normal way, setting Status=J.
 * </ol>
 *
 * <p>The hook takes care of the first two steps, while normal
 * processing does the third step.
 *
 * <p>The phinal phinesse is that we need to not make a phake recipient
 * when there is a real one (AFM-94). To solve this we require the RDF
 * property eph:has-sender-recipient to be set if the registry entry
 * has a real sender/recipient. So we check for that before making the
 * fake.
 */
public class RegistryEntryTypeUHook implements Hook {
    private static Logger log = LoggerFactory.getLogger(RegistryEntryTypeUHook.class.getName());
    private EphorteFacade facade;
    private String fake_recipient_name;
    private NCoreClient client;

    public void setFacade(EphorteFacade facade) {
        this.facade = facade;
        this.fake_recipient_name = facade.getConfigProperty("fake.recipient.name");
        if (fake_recipient_name == null)
            fake_recipient_name = "Fake recipient";
        this.client = facade.getClient();
    }

    public void run(Fragment fragment, Map<String, Object> ids) {
        // --- SHOULD WE DANCE?
        // we only deal with registry entries
        DataObjectT obj = fragment.getDataObject();
        if (!(obj instanceof RegistryEntryT))
            return;

        // if the type is already set to "J" we have nothing to do
        RegistryEntryT entry = (RegistryEntryT) obj;
        if (entry.getRegistryEntryTypeId() != null &&
            entry.getRegistryEntryTypeId().equals("R"))
            // if it's set to "J" we need to keep overriding it to "R"
            // until the recipient can be added
            return;

        // we only handle entries where type is "U"
        Statement s = fragment.getStatementWithSuffix("/registry-entry-type-id");
        if (s == null || !s.object.equals("U"))
            return;

        log.debug("Running hook on {}", fragment.getResourceId());

        // --- INTERLUDE TO AVOID CREATING UNNECESSARY FAKES
        Statement s2 = fragment.getStatementWithSuffix("/has-sender-recipient");
        if (s2 != null) {
            // there is a real sender/recipient. we don't need a fake one
            log.debug("There is a real sender/recipient. Not making a fake.");

            // we now change the RDF so that status is set to "R". a hook
            // on the SenderRecipientT will set it back to "J" (or whatever
            // it should be), once the SenderRecipientT is created.
            s = fragment.getStatementWithSuffix("/record-status-id");
            s.object = "R";
            return;
        }

        // --- DANCE, SISTER, DANCE
        // STEP 1: first attempt at entry
        // set initial state
        facade.populate(fragment, ids); // set state as originally defined
        entry.setRecordStatusId("R");   // override status

        // send initial SOAP request
        Object eId = ObjectUtils.invokeGetter(entry, "getId");
        if (eId == null)
            client.insert(entry);
        else
            client.update(entry);

        // STEP 2: make sender
        if (eId == null || entry.getSenderRecipient() == null) {
            // only making the sender if the entry is new, or it doesn't
            // have a sender
            SenderRecipientT sender = new SenderRecipientT();
            sender.setName(fake_recipient_name);
            sender.setRegistryEntryId(entry.getId());
            log.debug("Making sender", sender);
            client.insert(sender);
        }

        // STEP 3: update entry
        entry.setSenderRecipient(fake_recipient_name);
        client.update(entry);

        // STEP 4: set Status=J (done by normal processing)
        log.debug("Done");
    }
}
