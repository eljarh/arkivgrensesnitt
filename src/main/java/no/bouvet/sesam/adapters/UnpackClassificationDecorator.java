package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.ClassificationT;

// FIXME: should be rewritten so that it's not specific to ClassificationT

public class UnpackClassificationDecorator implements Decorator {
    @Override
    public Object process(DataObjectT obj, EphorteFacade facade, BatchFragment fragment, Statement s) {
        if (((CaseT) obj).getPrimaryClassification() != null)
            return null; // FIXME: we should really update it
      
        ClassificationT ct = new ClassificationT();

        String[] parts = s.object.split("::");
        for (String part : parts) {
            String[] kvs = part.split("=");
            ObjectUtils.setFieldValue(ct, kvs[0], kvs[1]);
        }

        return ct;
    }
}
