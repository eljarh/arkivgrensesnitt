package no.bouvet.sesam.adapters;

import org.apache.commons.io.IOUtils;
import java.io.InputStream;

public class Utils {
    public static String getResourceAsString(String name) throws Exception {
        InputStream is = getResource(name);
        return new String(IOUtils.toByteArray(is));
    }

    public static InputStream getResource(String name) {
        return Utils.class.getClassLoader().getResourceAsStream(name);
    }

    public static String getFirstSubject(String rdf) {
        int end = rdf.indexOf(">");
        return rdf.substring(1, end);
    }
}
