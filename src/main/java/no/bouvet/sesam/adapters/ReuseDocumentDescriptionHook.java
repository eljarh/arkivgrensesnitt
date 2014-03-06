
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
 * This hook solves the issue that ePhorte automatically creates a
 * document description when we make a registry entry (AFM-84). It
 * does that by picking up the document description that ePhorte
 * creates and reusing it.
 *
 * <p>For now, it simply searches to find that dd. If this turns out
 * to be too slow we may have to detect these when the re is created,
 * and store the IDs internally.
 */
public class ReuseDocumentDescriptionHook implements Hook {
    private static Logger log = LoggerFactory.getLogger(ReuseDocumentDescriptionHook.class.getName());
    private EphorteFacade facade;
    private NCoreClient client;

    public void setFacade(EphorteFacade facade) {
        this.facade = facade;
        this.client = facade.getClient();
    }

    public void run(Fragment fragment, Map<String, Object> ids) {
        DataObjectT obj = fragment.getDataObject();
        if (!(obj instanceof DocumentDescriptionT))
            return; // we're not interested

        DocumentDescriptionT orig = (DocumentDescriptionT) obj;
        Object id = orig.getId();
        if (id != null) {
            log.trace("Document description already exists, id {}", id);
            return; // the document description exists already, so we're OK
        }

        // so, let's see if there is a free document description we can use
        log.trace("Looking for a reusable DD for {}",
                  fragment.getResourceId());
        Statement s = fragment.getStatementWithSuffix("/registry-entry-reference");
        if (s == null) {
            log.trace("Giving up, can't find registry entry reference");
            return; // we can't do this
        }
        String re_uri = s.object;
        RegistryEntryT re = (RegistryEntryT)
            facade.get("RegistryEntry",
                       "CustomAttribute5=" + facade.encodeExternalId(re_uri));
        if (re == null) {
            log.trace("Giving up, no registry entry for {}", re_uri);
            return; // no go
        }

        List objs = client.get("RegistryEntryDocument",
                               "RegistryEntryId=" + re.getId());
        if (objs.isEmpty()) {
            log.trace("Giving up, no registry entry document for {}", re.getId());
            return;
        }

        RegistryEntryDocumentT red = (RegistryEntryDocumentT) objs.get(0);
        DocumentDescriptionT dd = (DocumentDescriptionT)
            facade.get("DocumentDescription",
                       "Id=" + red.getDocumentDescriptionId());
        if (dd == null) {
            log.trace("Giving up, no document description with id {}",
                      red.getDocumentDescriptionId());
            return;
        }

        if (dd.getCustomAttribute4() != null) {
            log.trace("Document description was created by us, can't reuse");
            return;
        }
        if (dd.getDocumentTitle() == null ||
            !dd.getDocumentTitle().equals(re.getTitle())) {
            log.trace("Document title for {} is wrong {}",
                      dd.getId(),
                      dd.getDocumentTitle());
            return;
        }

        objs = client.get("DocumentObject",
                          "DocumentDescriptionId=" + dd.getId());
        if (!objs.isEmpty()) {
            log.trace("Document versions for {} exist", dd.getId());
            return;
        }

        // okay, we're ready. there is a document description here that we
        // can reuse.
        log.trace("Reusing document description {} for regent {}",
                  dd.getId(), re.getId());
        fragment.setDataObject(dd);
    }
    
}
