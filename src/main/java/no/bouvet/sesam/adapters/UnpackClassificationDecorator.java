package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.ClassificationT;

public class UnpackClassificationDecorator implements Decorator {
    @Override
    public Object process(EphorteFacade facade, BatchFragment fragment, Statement s) {
        ClassificationT obj = new ClassificationT();

        String[] parts = s.object.split("::");
        for (String part : parts) {
            String[] kvs = part.split("=");
            ObjectUtils.setFieldValue(obj, kvs[0], kvs[1]);
        }

        return obj;
    }
}
