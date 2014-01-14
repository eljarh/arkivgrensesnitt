package no.bouvet.sesam.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import no.gecko.ncore.client.core.NCore;

import no.priv.garshol.duke.utils.ObjectUtils;

public class EphorteFacade {
    static Logger log = LoggerFactory.getLogger(EphorteFacade.class.getName());

    public static DataObjectT create(String typeName, String externalId) throws Exception {
        DataObjectT o = (DataObjectT) ObjectUtils.instantiate(typeName);
        setExternalId(o, externalId);
        return o;
    }

    public static void setExternalId(DataObjectT obj, String externalId) {
        setFieldValue(obj, "custom-attribute-2", externalId);
    }

    public static void populate(DataObjectT obj, List<Statement> statements) throws Exception {
        for (Statement s : statements) {
            populate(obj, s);
        }
    }

    public static void populate(DataObjectT obj, Statement s) throws Exception {
        String name = RDFMapper.getFieldName(s.property);
        String fieldType = RDFMapper.getFieldType (obj, name);
        if (fieldType == null) {
            log.debug("Object has no setter for {}", name);
            return;
        }

        if (!s.literal) {
            if (RDFMapper.isEphorteType(fieldType)) {
                DataObjectT o = get(fieldType, s.object);
                if (o == null) {
                    throw new RuntimeException("Refering to non-existing object: " + s.toString());
                }

                log.debug("Setting value of {} to {}", name, o);
                setFieldValue(obj, name, o);
                return;
            }
        }

        log.debug("Setting value of {} to {}", name, s.object);
        setFieldValue(obj, name, s.object);
    }
    public static DataObjectT get(String typeName, String externalId) throws Exception {
        String searchName = getSearchName(typeName);
        String query = getSearchString(typeName, externalId);
        List<DataObjectT> results = NCore.Objects.filteredQuery(searchName, query, new String[] {}, null, null);

        int found = results.size();

        if (found == 0)
            return null;
        if (found == 1)
            return results.get(0);

        throw new RuntimeException("Found multiple " + typeName + "s with external id = " + externalId);
    }

    public static void setFieldValue(DataObjectT obj, String name, String value) {
        ObjectUtils.setBeanProperty(obj, name, value, null);
    }

    public static void setFieldValue(DataObjectT obj, String name, DataObjectT value) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put(name, value);

        ObjectUtils.setBeanProperty(obj, name, name, m);
    }

    public static String getSearchName(String typeName) {
        int lastPeriod = typeName.lastIndexOf(".");
        return typeName.substring(lastPeriod + 1, typeName.length() - 1);
    }

    public static String getSearchString(String typeName, String externalId) {
        return getAttributeName(typeName) + "=" + externalId;
    }

    public static String getAttributeName(String typeName) {
        return "CustomAttribute2";
    }
}
