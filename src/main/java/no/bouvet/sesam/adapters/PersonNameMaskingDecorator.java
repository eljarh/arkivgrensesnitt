package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

/**
 * The names of job applicants need to be removed so that they are not
 * visible to the wrong users in ePhorte. WebCruiter indicates these
 * with @, so we remove all the non-space characters after that.
 *
 * <p>Note that this needs to be done in all versions of the title, or
 * ePhorte will complain that they don't have the same number of
 * words.
 *
 * <p>This decorator is in the wrong place. This should really be
 * taken care of in the mapping, but that's not really possible at
 * present. Will fix when it's possible.
 */
public class PersonNameMaskingDecorator implements Decorator {
    
    public void setFacade(EphorteFacade facade) {
        // don't need it
    }

    /**
     * We find the '@' character, rewrite the string, then return the
     * modified version.
     */
    public Object process(Fragment fragment, Statement statement) {
        int pos = statement.object.indexOf('@');
        if (pos != -1)
            return statement.object.substring(0, pos);
        else
            return statement.object;
    }
}
