package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

/**
 * A value preprocessor attached to a specific RDF property.
 */
public interface Decorator {

    /**
     * Called by EphorteFacade before the first call to process().
     */
    public void setFacade(EphorteFacade facade);

    /**
     * Pre-processes the value of the statement.object to produce a
     * value we will use instead of statement.object. This value is
     * the return value of process().
     */
    public Object process(Fragment fragment, Statement statement);
}
