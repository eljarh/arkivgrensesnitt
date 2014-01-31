package no.bouvet.sesam.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Iterator;
import java.util.Arrays;
import org.apache.commons.lang.WordUtils;

public class EphorteFacade {
    private static Logger log = LoggerFactory.getLogger(EphorteFacade.class.getName());
    private static EphorteFacade singleton = new EphorteFacade();

    private NCoreClient client;
    private String storageId;
    private String externalIdName;
    private String externalIdSearchName;
    private String rdfKeywordsName;

    private Map<String, Decorator> decorators = new HashMap<String, Decorator>();
    private Set<String> ignoredPrefixes = new HashSet();

    public EphorteFacade() {
        client = new NCoreClient();
        loadConfiguration();
    }

    public EphorteFacade(NCoreClient client) {
        this.client = client;
        loadConfiguration();
    }

    protected void loadConfiguration() {
        PropertiesConfiguration config = loadConfig("ephorte.properties");
        PropertiesConfiguration decorators = loadConfig("decorators.properties");

        init(config, decorators);
    }

    protected PropertiesConfiguration loadConfig(String name) {
        try {
            return new PropertiesConfiguration(name);
        } catch (Exception e) {
            log.error("Couldn't load {}", name, e);
            throw new RuntimeException(e);
        }

    }

    protected void init(PropertiesConfiguration config, PropertiesConfiguration decorators) {
        externalIdName = (String) config.getProperty("ephorte.externalId.name");
        externalIdSearchName = WordUtils.capitalize(externalIdName, new char[] { '-' }).replace("-", "");
        rdfKeywordsName = (String) config.getProperty("ephorte.rdfKeywords.name");
        storageId = (String) config.getProperty("ephorte.storageId");

        String prefixes = (String) config.getProperty("ephorte.ignoredReferencePrefixes");
        if (prefixes != null)
            for (String prefix : prefixes.split(","))
                addIgnoredReferencePrefix(prefix);

        Iterator<String> keys = decorators.getKeys();
        while(keys.hasNext()) {
            String klass = keys.next();
            String property = (String) decorators.getProperty(klass);
            Decorator d = (Decorator) ObjectUtils.instantiate(klass);
            setDecorator(property, d);
        }
    }

    public void setDecorator(String key, Decorator obj) {
        decorators.remove(key);
        decorators.put(key, obj);
    }

    public void addIgnoredReferencePrefix(String prefix) {
        ignoredPrefixes.add(prefix);
    }

    public static EphorteFacade getInstance() { return singleton; };

    public DataObjectT[] save(BatchFragment batch) throws Exception {
        Map<String, Object> ePhorteIds = new HashMap<String, Object>();

        List<DataObjectT> result = new ArrayList<DataObjectT>();

        for (Fragment f : batch.getFragments()) {
            DataObjectT r = save(f, ePhorteIds);
            result.add(r);
        }
        return result.toArray(new DataObjectT[0]);
    }

    public DataObjectT save(Fragment fragment) throws Exception {
        return save(fragment, new HashMap<String, Object>());
    }

    public DataObjectT save(Fragment fragment, Map<String, Object> ePhorteIds) throws Exception {
        String type = fragment.getType();
        if (StringUtils.isBlank(type)) {
            throw new InvalidFragment("Fragment has no type");
        }
        String ePhorteType = getObjectType(type);

        String resourceId = fragment.getResourceId();
        if (StringUtils.isBlank(resourceId)) {
           throw new InvalidFragment("Fragment has no resourceId");
        }

        log.debug("Looking up object with type {} and resourceId {}", ePhorteType, resourceId);
        DataObjectT obj = get(ePhorteType, resourceId);

        boolean objectExists = obj != null;
        if (!objectExists) {
            log.debug("Creating object with type {} and resourceId {}", ePhorteType, resourceId);
            obj = create(ePhorteType, resourceId);
        }

        setRdfKeywords(obj, fragment.getSource());
        populate(obj, fragment.getStatements(), ePhorteIds);

        if (objectExists) {
            client.update(obj);
            log.info("Updated resource: {}", fragment.getResourceId());
        } else {
            client.insert(obj);
            Object oId = ObjectUtils.invokeGetter(obj, "getId");
            log.info("Created resource: {} (ePhorteId={})", fragment.getResourceId(), oId);
            if (oId != null) {
                ePhorteIds.put(resourceId, oId);
            }
        }

        return obj;
    }

    public DataObjectT create(String typeName, String externalId) throws Exception {
        DataObjectT o = (DataObjectT) ObjectUtils.instantiate(typeName);
        setExternalId(o, externalId);
        return o;
    }

    public void setExternalId(DataObjectT obj, String externalId) {
        ObjectUtils.setFieldValue(obj, externalIdName, externalId);
    }


    public void setRdfKeywords(DataObjectT obj, String source) throws Exception {
        String link = uploadFile("rdfKeywords", source.getBytes("UTF-8"));
        ObjectUtils.setFieldValue(obj, rdfKeywordsName, link);
    }

    public void populate(DataObjectT obj, List<Statement> statements, Map<String, Object> ePhorteIds) throws Exception {
        for (Statement s : statements) {
            populate(obj, s, ePhorteIds);
        }
    }

    public void populate(DataObjectT obj, List<Statement> statements) throws Exception {
        populate(obj, statements, new HashMap<String, Object>());
    }


    public void populate(DataObjectT obj, Statement statement) throws Exception {
        populate(obj, statement, new HashMap<String, Object>());
    }

    public void populate(DataObjectT obj, Statement s, Map<String, Object> ePhorteIds) throws Exception {
        String name = getFieldName(s.property);

        String fieldType = ObjectUtils.getFieldType (obj, name);
        if (fieldType == null) {
            log.debug("Object has no setter for {}", name);
            return;
        }

        String value = s.object;
        if (decorators.containsKey(s.property)) {
            Decorator d = decorators.get(s.property);
            value = d.process(this, value);
        }

        if (isEphorteType(fieldType)) {
            if (!acceptedReference(value))
                return; // we're not going to set this reference

            Object oId = ePhorteIds.get(s.object);

            if (oId == null) {
                DataObjectT o = get(fieldType, value);
                if (o == null) {
                    String msg = String.format("Fragment <%s> tries to set property <%s> to non-existent object <%s>", s.subject, s.property, s.object);
                    throw new ReferenceNotFound(msg);
                }

                oId = ObjectUtils.invokeGetter(o, "getId");
            }

            if (oId == null) {
                String msg = String.format("Fragment <%s> tries to set property <%s> to object <%s>, however we can't get the objects id", s.subject, s.property, s.object);
                throw new InvalidReference(msg);
            }

            String idName = name + "-id";
            if (!ObjectUtils.hasField(obj, idName)) {
                String msg = String.format("Fragment <%s> tries to set property <%s> to object <%s>, however subject has no setter <%s>", s.subject, s.property, s.object, idName);
                throw new InvalidReference(msg);
            }

            ObjectUtils.setFieldValue(obj, idName, oId);
        } else {
            ObjectUtils.setFieldValue(obj, name, value);
        }
        return;
    }

    public DataObjectT get(String typeName, String externalId) throws Exception {
        String searchName = getSearchName(typeName);
        String query = getExternalIdSearchString(typeName, externalId);
        List<DataObjectT> results = client.get(searchName, query);

        if (results.size() == 0) {
            query = getEphorteIdSearchString(typeName, externalId);
            if (query != null) // externalId may be rejected
                results = client.get(searchName, query);
        }

        DataObjectT newest = null;
        XMLGregorianCalendar newestCreated = null;
        for (DataObjectT candidate : results) {
            XMLGregorianCalendar candidateCreated =
                (XMLGregorianCalendar) ObjectUtils.invokeGetter(candidate, "getCreatedDate");

            if (newestCreated == null || candidateCreated.compare(newestCreated) > 0) {
                newest = candidate;
                newestCreated = candidateCreated;
            }
        }

        return newest;
    }

    public static String getSearchName(String typeName) {
        int lastPeriod = typeName.lastIndexOf(".");
        return typeName.substring(lastPeriod + 1, typeName.length() - 1);
    }

    public String getExternalIdSearchString(String typeName, String externalId) {
        return externalIdSearchName + "=" + externalId;
    }

    public static String getEphorteIdSearchString(String typeName, String psi) {
        // ePhorte phreaks out if Id is not a number. we therefore
        // verify that before proceeding.
        String id = getFieldName(psi);
        if (!isInt(id))
            return null; // search will crash

        // now go ahead and make the query
        return "Id=" + id;
    }

    public static boolean isEphorteType(String typeName) {
        return typeName.startsWith("no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.");
    }

    public static String getObjectType(String property) {
        if (property == null) return "";

        String name = Utils.getLastPart(property);
        return "no.gecko.ephorte.services.objectmodel.v3.en.dataobjects." + name;
    }

    public static String getFieldName(String property) {
        if (property == null) return "";

        return Utils.getLastPart(property);
    }

    public String uploadFile(String fileName, byte[] data) throws Exception {
        return client.upload(fileName, storageId, data);
    }

    private boolean acceptedReference(String uri) {
        for (String prefix : ignoredPrefixes)
            if (uri.startsWith(prefix))
                return false; // we need to ignore this reference
        return true; // doesn't match anything, so is accepted
    }

    private static boolean isInt(String s) {
        for (int ix = 0; ix < s.length(); ix++) {
            char ch = s.charAt(ix);
            if (ch < '0' || ch > '9')
                return false;
        }
        return true;
    }
}
