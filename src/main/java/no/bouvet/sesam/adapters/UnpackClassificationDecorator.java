package no.bouvet.sesam.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.ClassificationT;

// FIXME: should be rewritten so that it's not specific to ClassificationT

// FIXME: unfortunately, this still doesn't work. see AFM-59.

public class UnpackClassificationDecorator implements Decorator {
    private static Logger log = LoggerFactory.getLogger(UnpackClassificationDecorator.class.getName());

    public void setFacade(EphorteFacade facade) {
        // don't need it
    }
    
    @Override
    public Object process(Fragment fragment, Statement s) {
        ClassificationT ct = new ClassificationT();

        String[] parts = s.object.split("::");
        for (String part : parts) {
            String[] kvs = part.split("=");
            ObjectUtils.setFieldValue(ct, kvs[0], kvs[1]);
        }

        return ct;
    }
}
