package no.bouvet.sesam.adapters;

import org.apache.commons.io.IOUtils;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

    public static Set<String> getAllSubjects(String rdf) {
        String lines[] = rdf.split("\\r?\\n");
        Set<String> subjects = new HashSet<String>();

        for (String line : lines) {
            String subject = getFirstSubject(line);
            subjects.add(subject);
        }

        return subjects;
    }

    public static String getLastPart(String property) {
        String[] parts = property.split("/");
        return parts[parts.length - 1];
    }

    // used by the ZipCodeDecorator and PhoneNumberDecorator
    public static String chopDigitOnlyValue(String value, int max) {
        if (value.length() <= max)
            return value; // no need to do anything

        char[] tmp = new char[max];
        int pos = 0;
        for (int ix = 0; ix < value.length() && pos < max; ix++) {
            char ch = value.charAt(ix);
            if (ch >= '0' && ch <= '9')
                tmp[pos++] = ch;
        }

        return new String(tmp, 0, pos);
    }
}
