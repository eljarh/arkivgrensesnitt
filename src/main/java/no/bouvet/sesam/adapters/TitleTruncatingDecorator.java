package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

/**
 * ePhorte rejects titles longer than 253 characters (AFM-103), so we
 * need to truncate them where they are longer.  Note that ePhorte
 * claims the limit is 255, but that's not true, as titles with
 * lengths 255 and 254 cause it to crash.  Clearly some phurther
 * complications are hidden deep within the bowels of ePhorte, causing
 * some titles to be lengthened so that Oracle rejects them. A title
 * of 242 characters phailed, but changing a '-' to a ' ' made it go
 * through. Experimentation shows that titles consisting purely of '-'
 * or '*' fail at 242, 241, and 240 characters, but work at 239. So
 * the limit is 239.
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
        if (title.length() > 239)
            return title.substring(0, 239);
        else
            return title;
    }
}
