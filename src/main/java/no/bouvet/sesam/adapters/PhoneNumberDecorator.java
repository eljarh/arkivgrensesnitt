
package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

/**
 * Ephorte rejects all phone numbers longer than 20 characters
 * (AFM-101).  Since this problem is general, and not related to
 * WebCruiter, we fix it with this decorator.  If the number is
 * shorter than 20 characters we don't do anything.  If it is longer,
 * we first strip all non-digit characters, then chop at 20.
 */
public class PhoneNumberDecorator implements Decorator {
    
    public void setFacade(EphorteFacade facade) {
        // we don't need it
    }

    public Object process(Fragment fragment, Statement s) {
        return Utils.chopDigitOnlyValue(s.object, 20);
    }
}
