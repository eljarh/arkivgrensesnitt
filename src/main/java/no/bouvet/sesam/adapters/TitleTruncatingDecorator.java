package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

/**
 * ePhorte rejects titles longer than 255 characters, so we need to
 * truncate them where they are longer.  (AFM-103)
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
        if (title.length() > 255)
            return title.substring(0, 255);
        else
            return title;
    }
}
