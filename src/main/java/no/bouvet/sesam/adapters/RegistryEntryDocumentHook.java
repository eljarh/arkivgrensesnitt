
package no.bouvet.sesam.adapters;

import java.util.Map;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.RegistryEntryT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.DocumentDescriptionT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.RegistryEntryDocumentT;

/**
 * In order to solve the problem that it's impossible to look up
 * document descriptions (DD) before they have a registry entry
 * document (RED) we don't make RDF for the RED. Instead, we send a
 * reference directly from the DD to the RE.
 *
 * <p>This post-hook takes care of creating the RED and wiring it up
 * correctly. That way we avoid the catch-22 where the DD is created
 * in a different batch from the RED, and so the RED can never be
 * created because it will never find its DD because the DD doesn't
 * have an RED. (This is AFM-91.)
 *
 * <p>Made this as a hook since we can't create the RED before the DD
 * is created.
 */
public class RegistryEntryDocumentHook implements Hook {
    private static Logger log = LoggerFactory.getLogger(RegistryEntryDocumentHook.class.getName());
    private EphorteFacade facade;
    private NCoreClient client;

    public void setFacade(EphorteFacade facade) {
        this.facade = facade;
        this.client = facade.getClient();
    }
    
    @Override
    public void run(Fragment fragment, Map<String, Object> ids) {
        // first, verify that we're working with a document description
        DataObjectT obj = fragment.getDataObject();
        if (!(obj instanceof DocumentDescriptionT))
            return;

        // then, check if the registry entry document already exists
        DocumentDescriptionT dd = (DocumentDescriptionT) obj;
        Object ddid = ObjectUtils.invokeGetter(dd, "getId");
        if (ddid != null) {
            // the DD already exists, so the RED might, too
            log.trace("DocumentDescription already exists, id {}", ddid);
            List objs = client.get("RegistryEntryDocument", "DocumentDescriptionId=" + ddid);
            log.trace("Searching for RegistryEntryDocuments found {}", objs.size());
            if (!objs.isEmpty())
                return; // the RED is already there
        } else {
            // this shouldn't ever happen, since we just created/updated it
            log.error("DocumentDescription does not exist already");
            return;
        }

        // okay, there is no registry entry document. find the registry entry
        Statement s = fragment.getStatementWithSuffix("/registry-entry-reference");
        if (s == null) {
            log.trace("Couldn't find statement");
            return;
        }
        String re_uri = s.object;
        RegistryEntryT re = (RegistryEntryT) facade.getById("RegistryEntryT", re_uri);
        if (re == null) {
            log.error("Couldn't find RegistryEntry {}", re_uri);
            return;
        }

        // finally, we're ready
        RegistryEntryDocumentT red = new RegistryEntryDocumentT();
        red.setRegistryEntryId(re.getId());
        red.setDocumentDescriptionId(dd.getId());
        log.trace("Making RegistryEntryDocument pointing to {}", re.getId());
        client.insert(red);
    }
}
