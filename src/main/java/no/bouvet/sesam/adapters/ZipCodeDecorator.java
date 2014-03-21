
package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

/**
 * Ephorte rejects all zip codes longer than 5 characters (AFM-98).
 * This was first triggered by the zip code 'LV-1007'. Since this
 * problem is general, and not related to WebCruiter, we fix it with
 * this decorator. The decorator first strips all non-digit
 * characters. If the string is still longer than 5 characters we keep
 * only the first 5. We lose data, but what is a man to do?
 */
public class ZipCodeDecorator implements Decorator {
    
    public void setFacade(EphorteFacade facade) {
        // we don't need it
    }

    public Object process(Fragment fragment, Statement s) {
        return Utils.chopDigitOnlyValue(s.object, 5);
    }
}
