package no.bouvet.sesam.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Iterator;

public class EphorteFacade {
    private static Logger log = LoggerFactory.getLogger(EphorteFacade.class.getName());
    private static EphorteFacade singleton = new EphorteFacade();

    private NCoreClient client;
    private String storageId;
    private String externalIdName;
    private String rdfKeywordsName;

    private Map<String, Decorator> decorators = new HashMap<String, Decorator>();

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
        rdfKeywordsName = (String) config.getProperty("ephorte.rdfKeywords.name");
        storageId = (String) config.getProperty("ephorte.storageId");

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

    public static EphorteFacade getInstance() { return singleton; };

    public DataObjectT[] save(Fragment fragment) throws Exception {
        String type = fragment.getType();
        if (StringUtils.isBlank(type)) {
            throw new RuntimeException("Fragment has no type");
        }
        String ePhorteType = getObjectType(type);

        String resourceId = fragment.getResourceId();
        if (StringUtils.isBlank(resourceId)) {
           throw new RuntimeException("Fragment has no resourceId");
        }

        log.debug("Looking up object with type {} and resourceId {}", ePhorteType, resourceId);
        DataObjectT obj = get(ePhorteType, resourceId);

        boolean objectExists = obj != null;
        if (!objectExists) {
            log.debug("Creating object with type {} and resourceId {}", ePhorteType, resourceId);
            obj = create(ePhorteType, resourceId);
        }
        setRdfKeywords(obj, fragment.getSource());

        DataObjectT[] objs = populate(obj, fragment.getStatements());


        if (objectExists) {
            client.update(objs);
            log.info("Updated resource: {}", fragment.getResourceId());
        } else {
            client.insert(objs);
            log.info("Created resource: {}", fragment.getResourceId());
        }

        return objs;
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
        String name = getFieldName(s.property);

        String fieldType = ObjectUtils.getFieldType (obj, name);
        if (fieldType == null) {
            log.debug("Object has no setter for {}", name);
            return null;
        }

        String value = s.object;
        if (decorators.containsKey(s.property)) {
            Decorator d = decorators.get(s.property);
            value = d.process(value);
        }

        if (!s.literal) {
            if (isEphorteType(fieldType)) {
                DataObjectT o = get(fieldType, value);
                if (o == null) {
                    String msg = String.format("Fragment tries to set property <%s> to non-existent object <%s>", s.property, s.object);
                    throw new ReferenceNotFound(msg);
                }

                ObjectUtils.setFieldValue(obj, name, o);
                return o;
            }
        }

        ObjectUtils.setFieldValue(obj, name, value);
        return null;
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

    public static String getExternalIdSearchString(String typeName, String externalId) {
        return "CustomAttribute2=" + externalId;
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

    private static boolean isInt(String s) {
        for (int ix = 0; ix < s.length(); ix++) {
            char ch = s.charAt(ix);
            if (ch < '0' || ch > '9')
                return false;
        }
        return true;
    }
}
