package no.bouvet.sesam.adapters;

public class RDFMapper {
    public static String lookupObjectType(String property) {
        if (property == null) return "";

        String[] parts = property.split("/");
        return "no.gecko.ephorte.services.objectmodel.v3.en.dataobjects." + parts[parts.length - 1];
    }
}
