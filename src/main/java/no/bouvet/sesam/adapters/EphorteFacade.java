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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

public class EphorteFacade {
    private static Logger log = LoggerFactory.getLogger(EphorteFacade.class.getName());
    private static String externalIdName;
    private static String rdfKeywordsName;
    private static EphorteFacade singleton = new EphorteFacade();

    public EphorteFacade () { }

    public static EphorteFacade getInstance() { return singleton; };

    static {
        try {
            PropertiesConfiguration config = new PropertiesConfiguration("ephorte.properties");
            externalIdName = (String) config.getProperty("ephorte.externalId.name");
            rdfKeywordsName = (String) config.getProperty("ephorte.rdf-keywords.name");
        } catch (Exception e) {
            log.error("Couldn't load ephorte.properties", e);
        }
    }

    public void save(Fragment fragment) throws Exception {
        String type = fragment.getType();
        if (StringUtils.isBlank(type)) {
            throw new RuntimeException("Fragment has no type");
        }

        String resourceId = fragment.getResourceId();
        if (StringUtils.isBlank(resourceId)) {
           throw new RuntimeException("Fragment has no resourceId");
        }

        log.debug("Looking up object with type {} and resourceId {}", type, resourceId);
        DataObjectT obj = get(type, resourceId);

        boolean objectExists = obj != null;
        if (!objectExists) {
            log.debug("Creating object with type {} and resourceId {}", type, resourceId);
            obj = create(type, resourceId);
        }

        DataObjectT[] objs = populate(obj, fragment.getStatements());

        /// There's a limit of 255 chars for the custom attributes.
        /// In other words, this breaks.
        // setRdfKeywords(obj, fragment.getSource());

        if (objectExists) {
            NCore.Objects.update(objs);
            log.info("Updated resource: {}", fragment.getResourceId());
        } else {
            NCore.Objects.insert(objs);
            log.info("Created resource: {}", fragment.getResourceId());
        }
    }

    public DataObjectT create(String typeName, String externalId) throws Exception {
        DataObjectT o = (DataObjectT) ObjectUtils.instantiate(typeName);
        setExternalId(o, externalId);
        return o;
    }

    public static void setExternalId(DataObjectT obj, String externalId) {
        setFieldValue(obj, externalIdName, externalId);
    }

    public static void setRdfKeywords(DataObjectT obj, String source) {
        setFieldValue(obj, rdfKeywordsName, source);
    }

    public DataObjectT[] populate(DataObjectT obj, List<Statement> statements) throws Exception {
        List<DataObjectT> objs = new ArrayList<DataObjectT>();
        for (Statement s : statements) {
            DataObjectT referencedObject = populate(obj, s);
            if (referencedObject != null)
                objs.add(referencedObject);
        }
        objs.add(obj);
        return objs.toArray(new DataObjectT[objs.size()]);
    }

    public DataObjectT populate(DataObjectT obj, Statement s) throws Exception {
        String name = RDFMapper.getFieldName(s.property);
        String fieldType = RDFMapper.getFieldType (obj, name);
        if (fieldType == null) {
            log.debug("Object has no setter for {}", name);
            return null;
        }

        if (!s.literal) {
            if (RDFMapper.isEphorteType(fieldType)) {
                DataObjectT o = get(fieldType, s.object);
                if (o == null) {
                    throw new RuntimeException("Refering to non-existing object: " + s.toString());
                }

                setFieldValue(obj, name, o);
                return o;
            }
        }

        setFieldValue(obj, name, s.object);
        return null;
    }

    public DataObjectT get(String typeName, String externalId) throws Exception {
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
        log.debug("Setting value of {} to {}", name, value);
        ObjectUtils.setBeanProperty(obj, name, value, null);
    }

    public static void setFieldValue(DataObjectT obj, String name, DataObjectT value) {
        log.debug("Setting value of {} to {}", name, value);
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
