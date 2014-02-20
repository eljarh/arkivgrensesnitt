package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

// FIXME: document with javadoc
public interface Decorator {

    public void setFacade(EphorteFacade facade);
    
    public Object process(Fragment fragment, Statement statement);
}
