package no.bouvet.sesam.adapters;

import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class ObjectUtils {
    static Logger log = LoggerFactory.getLogger(ObjectUtils.class.getName());

    public static void setFieldValue(Object obj, String name, String value) {
        log.debug("Setting value of {} to {}", name, value);
        String type = getFieldType(obj, name).getName();

        // Workaround for bug in no.priv.garshol.duke.utils.ObjectUtils.setBeanProperty
        if (type.equals("java.lang.Integer")) {
            setFieldValue(obj, name, Integer.parseInt(value));
        } else if (type.equals("java.lang.Boolean")) {
            setFieldValue(obj, name, Boolean.parseBoolean(value) ||
                                     value.equals("1"));
        } else {
            no.priv.garshol.duke.utils.ObjectUtils.setBeanProperty(obj, name, value, null);
        }
    }

    public static Object getFieldValue(Object obj, String name) throws Exception {
        String getterName = makeGetterName(name);
        return invokeGetter(obj, getterName);
    }

    public static void setFieldValue(Object obj, String name, Object value) {
        if (value instanceof String) {
            setFieldValue(obj, name, (String) value);
            return;
        }

        log.debug("Setting value of {} to {}", name, value);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put(name, value);

        no.priv.garshol.duke.utils.ObjectUtils.setBeanProperty(obj, name, name, m);
    }

    public static Object instantiate(String typeName) {
        return no.priv.garshol.duke.utils.ObjectUtils.instantiate(typeName);
    }

    public static Class getFieldType(Object obj, String fieldName) {
        String methodName = makePropertyName(fieldName);
        Method m = getMethod(obj, methodName);
        if (m == null) return null;

        Class type = m.getParameterTypes()[0];
        return type;
    }

    public static boolean hasField(Object obj, String fieldName) {
        String methodName = makePropertyName(fieldName);
        Method m = getMethod(obj, methodName);
        return m != null;
    }

    public static Object invokeGetter(Object obj, String getterName) {
        Method m = getMethod(obj, getterName);
        if (m == null) return null;

        try {
          return m.invoke(obj);
        } catch (Exception e) {
          throw new RuntimeException("Getter failed", e);
        }
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

    private static String makeGetterName(String name) {
        String setterName = makePropertyName(name);
        return "g" + setterName.substring(1,setterName.length());
    }

    private static Method getMethod(Object obj, String methodName) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getName().equals(methodName))
                return m;
        }

        return null;
    }
}
