package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

/**
 * ePhorte rejects titles longer than 253 characters, so we need to
 * truncate them where they are longer.  Note that ePhorte claims the
 * limit is 255, but that's not true, as titles with lengths 255 and
 * 254 cause it to crash. (AFM-103)
 */
public class TitleTruncatingDecorator implements Decorator {
    
    public void setFacade(EphorteFacade facade) {
        // don't need it
    }

    /**
     * Shorten title, if necessary
     */
    public Object process(Fragment fragment, Statement statement) {
        String title = statement.object;
        if (title.length() > 253)
            return title.substring(0, 253);
        else
            return title;
    }
}
