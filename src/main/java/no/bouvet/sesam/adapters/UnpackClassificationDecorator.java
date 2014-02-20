package no.bouvet.sesam.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.ClassificationT;

// This decorator is needed to work around a bug in the ePhorte API.
// When we fetch a CaseT that has a primary classification in ePhorte,
// the response from ePhorte still claims that the primary
// classification is null.

public class UnpackClassificationDecorator implements Decorator {
    private static Logger log = LoggerFactory.getLogger(UnpackClassificationDecorator.class.getName());

    EphorteFacade _facade;
    public void setFacade(EphorteFacade facade) {
        _facade = facade;
    }
    
    @Override
    public Object process(Fragment fragment, Statement s) {
        ClassificationT ct = getExistingClassification(fragment);
        if (ct == null) {
            return createClassification(s);
        }

        // If there exists a classification, we return null instead of
        // the existing classification.  This is because we don't want
        // to set it again.
        return null;
    }

    public ClassificationT getExistingClassification(Fragment fragment) {
        String resourceId = fragment.getResourceId();

        CaseT c = (CaseT) fragment.getDataObject();
        Integer caseId = c.getId();
        if (caseId == null) return null;

        return (ClassificationT) _facade.get("Classification", "CaseId=" + caseId);
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
