
package no.bouvet.sesam.adapters;

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
 * <p>This decorator takes care of creating the RED and wiring it up
 * correctly. That way we avoid the catch-22 where the DD is created
 * in a different batch from the RED, and so the RED can never be
 * created because it will never find its DD because the DD doesn't
 * have an RED. (This is AFM-91.)
 */
public class RegistryEntryDocumentDecorator implements Decorator {
    private static Logger log = LoggerFactory.getLogger(RegistryEntryDocumentDecorator.class.getName());
    private EphorteFacade facade;
    private NCoreClient client;

    public void setFacade(EphorteFacade facade) {
        this.facade = facade;
        this.client = facade.getClient();
    }
    
    @Override
    public Object process(Fragment fragment, Statement s) {
        // first, check if the registry entry document already exists
        DataObjectT obj = fragment.getDataObject();
        DocumentDescriptionT dd = (DocumentDescriptionT) obj;
        Object ddid = ObjectUtils.invokeGetter(dd, "getId");
        log.trace("DocumentDescription already exists, id {}", ddid);
        if (ddid != null) {
            // the DD already exists, so the RED might, too
            List objs = client.get("RegistryEntryDocumentT", "DocumentDescriptionId=" + ddid);
            log.trace("Searching for RegistryEntryDocuments found {}", objs.size());
            if (!objs.isEmpty())
                return null; // the RED is already there
        }

        // okay, there is no registry entry document. find the registry entry
        String re_uri = s.object;
        RegistryEntryT re = (RegistryEntryT) facade.getById("RegistryEntry", re_uri);
        if (re == null) {
            log.error("Couldn't find RegistryEntry {}", re_uri);
            return null;
        }

        // finally, we're ready
        RegistryEntryDocumentT red = new RegistryEntryDocumentT();
        red.setRegistryEntry(re);
        red.setDocumentDescription(dd);
        log.trace("Making RegistryEntryDocument pointing to {}", re.getId());
        client.insert(red);
        return null;
    }
}
