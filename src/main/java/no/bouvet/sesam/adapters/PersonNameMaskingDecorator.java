package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

/**
 * The names of job applicants need to be masked out so that they are
 * not visible to the wrong users in ePhorte. WebCruiter indicates
 * these with @, so we mask out all the non-space characters after
 * that.
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
        // we are not going to change the length, so this is safe
        char[] str = statement.object.toCharArray();

        // loop, masking once we're past the '@'
        boolean mask = false;
        for (int ix = 0; ix < str.length; ix++) {
            if (mask && str[ix] != ' ')
                str[ix] = '#';
            else if (!mask && str[ix] == '@')
                mask = true;
        }

        // we're done
        if (mask)
            return new String(str);
        else
            return statement.object;
    }
}
