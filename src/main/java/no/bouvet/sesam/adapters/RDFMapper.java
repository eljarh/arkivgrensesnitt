package no.bouvet.sesam.adapters;

public class RDFMapper {
    public static String getObjectType(String property) {
        if (property == null) return "";

        String name = getLastPart(property);
        return "no.gecko.ephorte.services.objectmodel.v3.en.dataobjects." + name;
    }

    public static String getFieldName(String property) {
        if (property == null) return "";

        return getLastPart(property);
    }

    public static String getResourceId(String value) {
        return getLastPart(value);
    }

    private static String getLastPart(String property) {
        String[] parts = property.split("/");
        return parts[parts.length - 1];
    }
}
