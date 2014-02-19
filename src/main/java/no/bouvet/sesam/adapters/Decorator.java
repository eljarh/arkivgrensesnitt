package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

// FIXME: document with javadoc
// FIXME: reconsider the process() method signature
public interface Decorator {
    Object process(DataObjectT obj, EphorteFacade facade, BatchFragment fragment, Statement statement) throws Exception;
}
