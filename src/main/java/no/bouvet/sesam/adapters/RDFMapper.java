package no.bouvet.sesam.adapters;

import java.lang.reflect.Method;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

public class RDFMapper {
    public static String getObjectType(String property) {
        if (property == null) return "";

        String name = getLastPart(property);
        return "no.gecko.ephorte.services.objectmodel.v3.en.dataobjects." + name;
    }

    public static String getFieldType(DataObjectT obj, String fieldName) {
        String methodName = makePropertyName(fieldName);
        Method m = getMethod(obj, methodName);
        if (m == null) return null;

        Class type = m.getParameterTypes()[0];
        return type.getName();
    }

    public static String getFieldName(String property) {
        if (property == null) return "";

        return getLastPart(property);
    }

    public static boolean isEphorteType(String typeName) {
        return typeName.startsWith("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.");
    }

    private static String getLastPart(String property) {
        String[] parts = property.split("/");
        return parts[parts.length - 1];
    }

    /*
      Ripped from no.priv.garshol.duke.utils.ObjectUtils.
      Unfortunately it was declared as private, so I decided to lift
      it verbatim for now.

      FIXME: Expose this method in Duke so we can use it.
     */
    private static String makePropertyName(String name) {
        char[] buf = new char[name.length() + 3];
        int pos = 0;
        buf[pos++] = 's';
        buf[pos++] = 'e';
        buf[pos++] = 't';

        for (int ix = 0; ix < name.length(); ix++) {
            char ch = name.charAt(ix);
            if (ix == 0)
                ch = Character.toUpperCase(ch);
            else if (ch == '-') {
                ix++;
                if (ix == name.length())
                    break;
                ch = Character.toUpperCase(name.charAt(ix));
            }

            buf[pos++] = ch;
        }

        return new String(buf, 0, pos);
    }

    private static Method getMethod(Object obj, String methodName) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getName().equals(methodName))
                return m;
        }

        return null;
    }
}
