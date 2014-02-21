package no.bouvet.sesam.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.ClassificationT;
import java.util.Map;

// This decorator is needed to work around a bug in the ePhorte API.
// When we fetch a CaseT that has a primary classification in ePhorte,
// the response from ePhorte still claims that the primary
// classification is null.

public class PrimaryClassificationHook implements Hook {
    private static Logger log = LoggerFactory.getLogger(PrimaryClassificationHook.class.getName());

    EphorteFacade facade;
    public void setFacade(EphorteFacade facade) {
        this.facade = facade;
    }

    @Override
    public void run(Fragment fragment, Map<String, Object> ids) {
        DataObjectT obj = fragment.getDataObject();
        if (!(obj instanceof CaseT))
            return;

        log.debug("Running PrimaryClassificationHook on {}", fragment.getResourceId());

        CaseT c = (CaseT) obj;
        ClassificationT ct = getExistingClassification(c);

        // If there exists a classification, we're done.
        if (ct != null) {
            log.debug("Classification already exists.");
            return;
        }

        Statement s = fragment.getStatement("http://data.mattilsynet.no/sesam/ephorte/primary-classification");

        // If the fragment doesn't contain any primary-classification
        // we're done.
        if (s == null)
            return;

        // Otherwise we create the classification, pointing to the
        // case in question.  Note that this relies on the case
        // existing in ePhorte.
        ct = createClassification(s);
        if (ct == null)
            return;

        ct.setCaseId(c.getId());

        NCoreClient client = facade.getClient();
        log.debug("Inserting classification.");
        client.insert(ct);
        log.debug("Done");
    }

    public ClassificationT getExistingClassification(CaseT c) {
        Integer caseId = c.getId();
        if (caseId == null) return null;

        return (ClassificationT) facade.get("Classification", "CaseId=" + caseId);
    }

    public ClassificationT createClassification(Statement s) {
        ClassificationT ct = new ClassificationT();

        String[] parts = s.object.split("::");
        for (String part : parts) {
            String[] kvs = part.split("=");
            ObjectUtils.setFieldValue(ct, kvs[0], kvs[1]);
        }

        return ct;
    }
}
