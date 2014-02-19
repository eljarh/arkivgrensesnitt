package no.bouvet.sesam.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.ClassificationT;

// FIXME: should be rewritten so that it's not specific to ClassificationT

public class UnpackClassificationDecorator implements Decorator {
    private static Logger log = LoggerFactory.getLogger(UnpackClassificationDecorator.class.getName());

    @Override
    public Object process(DataObjectT obj, EphorteFacade facade, BatchFragment fragment, Statement s) {
        // ePhorte complains if we try to add another ClassificationT to a
        // case that already has one. we therefore pass if there is a value
        ClassificationT ct = ((CaseT) obj).getPrimaryClassification();
        log.debug("CaseT.primaryClassification {}", ct);
        if (ct != null)
            return null; // FIXME: we should really update it
      
        ct = new ClassificationT();

        String[] parts = s.object.split("::");
        for (String part : parts) {
            String[] kvs = part.split("=");
            ObjectUtils.setFieldValue(ct, kvs[0], kvs[1]);
        }

        return ct;
    }
}
