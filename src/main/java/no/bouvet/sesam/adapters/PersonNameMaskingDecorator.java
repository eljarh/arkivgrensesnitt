package no.bouvet.sesam.adapters;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

/**
 * The names of job applicants need to be removed so that they are not
 * visible to the wrong users in ePhorte. WebCruiter indicates these
 * with @, so we remove the rest of the string.
 *
 * <p>Note that this needs to be done in all versions of the title, or
 * ePhorte will complain that they don't have the same number of
 * words.
 *
 * <p>This decorator is in the wrong place. This should really be
 * taken care of in the mapping, but that's not really possible at
 * present. Will fix when it's possible.
 *
 * <p>In addition, ePhorte has a bug in the handling of titles on
 * journal posts, in that it doesn't properly escape "'" characters in
 * the SQL queries it generates. We solve that here by removing them.
 * (This part of the decorator *is* in the right place.)
 *
 * <p>As iph that were not enouph, ePhorte rejects titles it deems too
 * long. How long is complicated subject. See TitleTruncatingDecorator
 * for the phull, sad story.
 */
public class PersonNameMaskingDecorator implements Decorator {
    
    public void setFacade(EphorteFacade facade) {
        // don't need it
    }

    /**
     * We find the '@' character, strip any apostrophes, then return
     * the modified version.
     */
    public Object process(Fragment fragment, Statement statement) {
        boolean modified = false;
        String str = statement.object;
        char[] tmp = new char[str.length()];

        int writeat = 0;
        int ix = 0;
        for (; ix < str.length(); ix++) {
            char ch = str.charAt(ix);
            if (ch == '@') {
                modified = true;
                break; // that's it, we're done
            } else if (ch == '\'')
                modified = true;
            else
                tmp[writeat++] = ch;
        }

        // put together the desired title
        String newtitle;
        if (modified)
            newtitle = new String(tmp, 0, writeat);
        else
            newtitle = statement.object;

        // truncate if necessary
        if (newtitle.length() > 239)
          newtitle = newtitle.substring(0, 239);

        // done
        return newtitle;
    }
}
