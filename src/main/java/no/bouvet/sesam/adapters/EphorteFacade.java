package no.bouvet.sesam.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.apache.commons.codec.binary.Base32;

public class EphorteFacade {
    private static Base32 codec = new Base32();
    private static Logger log = LoggerFactory.getLogger(EphorteFacade.class.getName());
    private static EphorteFacade singleton = new EphorteFacade();

    private NCoreClient client;
    private String storageId;
    private String externalIdName;
    private String externalIdNameEncoded;
    private String externalIdSearchName;
    private String rdfKeywordsName;

    private Map<String, Decorator> decorators = new HashMap<String, Decorator>();
    private Set<String> ignoredPrefixes = new HashSet();
    private Set<String> immutableProperties = new HashSet();
    private Collection<Hook> hooks = new ArrayList();

    public EphorteFacade() {
        this(true);
    }

    public EphorteFacade(NCoreClient client) {
        this.client = client;
        loadConfiguration();
    }

    // we need this for testing
    public EphorteFacade(boolean loadcfg) {
        client = new NCoreClient();
        if (loadcfg)
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
        externalIdNameEncoded = (String) config.getProperty("ephorte.externalId.name.encoded");
        externalIdSearchName = WordUtils.capitalize(externalIdNameEncoded, new char[] { '-' }).replace("-", "");
        rdfKeywordsName = (String) config.getProperty("ephorte.rdfKeywords.name");
        storageId = (String) config.getProperty("ephorte.storageId");

        Object v = config.getProperty("ephorte.ignoredReferencePrefixes");
        for (String prefix : decodeListProperty(v))
            addIgnoredReferencePrefix(prefix);

        v = config.getProperty("ephorte.immutableProperties");
        for (String property : decodeListProperty(v))
            addImmutableProperty(property);

        hooks.add(new RegistryEntryTypeUHook());
        for (Hook hook : hooks)
            hook.setFacade(this);
        
        Iterator<String> keys = decorators.getKeys();
        while(keys.hasNext()) {
            String klass = keys.next();
            String property = (String) decorators.getProperty(klass);
            Decorator d = (Decorator) ObjectUtils.instantiate(klass);
            setDecorator(property, d);
        }
    }

    // the genius of the designers of PropertiesConfiguration is such that
    // if the value contains a "," it becomes a Collection<String>, but if
    // it does not it's just a String. so you get runtime casting errors
    // the moment the user decides to have more than one value. it's enough
    // to make you despair of humanity. I would throw the garbage out if I
    // had time.
    private static Collection<String> decodeListProperty(Object v) {
        if (v == null) {
            return Collections.EMPTY_LIST;
        } else if (v instanceof String) {
            return (Collection) Collections.singleton(v);
        } else {
            return (Collection<String>) v;
        }
    }

    public void setDecorator(String key, Decorator obj) {
        decorators.remove(key);
        decorators.put(key, obj);
        obj.setFacade(this);
    }

    public NCoreClient getClient() {
        return client;
    }
    
    public Set<String> getImmutableProperties() {
        return immutableProperties;
    }

    public void addImmutableProperty(String property) {
        immutableProperties.add(property);
    }

    public void addIgnoredReferencePrefix(String prefix) {
        ignoredPrefixes.add(prefix);
    }

    public static EphorteFacade getInstance() { return singleton; };

    public DataObjectT[] save(BatchFragment batch) throws Exception {
        Map<String, Object> ePhorteIds = new HashMap<String, Object>();

        List<DataObjectT> result = new ArrayList<DataObjectT>();

        for (Fragment fragment : batch.getFragments()) {
            DataObjectT r = save(fragment, ePhorteIds);
            result.add(r);
        }
        return result.toArray(new DataObjectT[0]);
    }
    
    // FIXME: method name is wrong. this is not an overloaded version of the
    //          save() method above
    public DataObjectT save(Fragment fragment, Map<String, Object> ePhorteIds) throws Exception {
        fragment.validate();

        String resourceId = fragment.getResourceId();
        String ePhorteType = getObjectType(fragment.getType());

        log.debug("Looking up object with type {} and resourceId {}", ePhorteType, resourceId);
        DataObjectT obj = get(ePhorteType, resourceId);

        boolean objectExists = obj != null;
        if (!objectExists) {
            log.debug("Creating object with type {} and resourceId {}", ePhorteType, resourceId);
            obj = create(ePhorteType, resourceId);
        }
        fragment.setDataObject(obj);

        // run hooks
        for (Hook hook : hooks)
            hook.run(fragment, ePhorteIds);

        // it's possible that the hooks have now created the object
        if (!objectExists) {
            Object oId = ObjectUtils.invokeGetter(obj, "getId");
            if (oId != null) {
                log.info("Some hook created resource: {} (ePhorteId={})",
                         resourceId, oId);
                ePhorteIds.put(resourceId, oId);
                objectExists = true; // now we know we should update
            }
        }
        
        // FIXME: disabling this for now, so that we can finally get the
        // documents to be "hoveddokumenter"
        //setRdfKeywords(obj, fragment.getSource());

        // go through statements and populate the object
        Collection<DataObjectT> newobjs = populate(fragment, ePhorteIds);

        // now send SOAP request
        if (objectExists) {
            client.update(obj);
            log.info("Updated resource: {}", resourceId);
        } else {
            client.insert(obj);
            Object oId = ObjectUtils.invokeGetter(obj, "getId");
            log.info("Created resource: {} (ePhorteId={})", resourceId, oId);
            if (oId != null) {
                ePhorteIds.put(resourceId, oId);
            }
        }

        // did decorators create any new objects?
        if (!newobjs.isEmpty()) {
            // we need to also create the new objects created by
            // decorators, *and* wire into these objects references back
            // to the parent object 'obj'. we make this work by assuming
            // that if 'obj' is a CaseT, then the object created by the
            // decorator will have a 'setCaseId' method we can use.
            setParentReferences(obj, newobjs);
            client.insert(newobjs);
        }

        return obj;
    }

    private void setParentReferences(DataObjectT parent,
                                     Collection<DataObjectT> children)
        throws Exception {
        // parse out relevant part of name: "no...CaseT" => "Case"
        String fqcn = parent.getClass().getName();
        int pos = fqcn.lastIndexOf('.');
        String klass = fqcn.substring(pos + 1, fqcn.length() - 1);

        // precompute
        String property = klass + "-id";
        int id = (Integer) ObjectUtils.getFieldValue(parent, "id");

        // do the needful
        for (DataObjectT child : children)
            ObjectUtils.setFieldValue(child, property, id);
    }

    public DataObjectT create(String typeName, String externalId) {
        DataObjectT o = (DataObjectT) ObjectUtils.instantiate(typeName);
        setExternalId(o, externalId);
        return o;
    }

    public String encodeExternalId(String externalId) {
        return "BASE32:" + codec.encodeAsString(externalId.getBytes());
    }

    public void setExternalId(DataObjectT obj, String externalId) {
        String value = encodeExternalId(externalId);
        ObjectUtils.setFieldValue(obj, externalIdName, externalId);
        ObjectUtils.setFieldValue(obj, externalIdNameEncoded, value);
    }

    public void setRdfKeywords(DataObjectT obj, String source) throws Exception {
        String link = uploadFile("rdfKeywords", source.getBytes("UTF-8"));
        ObjectUtils.setFieldValue(obj, rdfKeywordsName, link);
    }

    public Collection<DataObjectT> populate(Fragment fragment, Map<String, Object> ePhorteIds) {
        Collection<DataObjectT> newobjs = new ArrayList();
        List<ReferenceNotFound> missingReferences = new ArrayList<ReferenceNotFound>();
        for (Statement s : fragment.getStatements()) {
            try {
                DataObjectT newO = populate(fragment, s, ePhorteIds);
                if (newO != null)
                    newobjs.add(newO);
            } catch (ReferenceNotFound e) {
                missingReferences.add(e);
            }
        }

        /* Make sure that the reference is still missing after
         * having processed the full fragment */
        for (ReferenceNotFound e : missingReferences) {
            Statement s = e.getStatement();
            String name = getReferenceFieldName(s.property);
            if (valueIsMissing(fragment.getDataObject(), name))
                throw e;
        }
        return newobjs;
    }

    public void populate(Fragment fragment) {
        populate(fragment, new HashMap<String, Object>());
    }

    public void populate(Fragment fragment, Statement statement) {
        populate(fragment, statement, new HashMap<String, Object>());
    }

    // returns the new value object created by decorator, if any
    public DataObjectT populate(Fragment fragment, Statement s, Map<String, Object> ePhorteIds) {
        DataObjectT obj = fragment.getDataObject();
        String name = getFieldName(s.property);

        if (immutableProperties.contains(s.property)) {
            if (hasValue(obj, name)) {
                log.debug("Object already has value for immutable property: {}", s.property);
                return null;
            } else
                log.debug("Property {} is immutable, but it has no value", s.property);
        }

        String fieldType = ObjectUtils.getFieldType (obj, name);
        if (fieldType == null) {
            log.debug("Object has no setter for {}", name);
            return null;
        }

        if (isEphorteType(fieldType) && !acceptedReference(s.object)) {
            log.debug("Value is not acceptable reference: {}", s.object);
            return null;
        }

        if (isEphorteType(fieldType) && !decorators.containsKey(s.property)) {
            String idName = getReferenceFieldName(s.property);
            Object oId = getIdValue(obj, fieldType, s, ePhorteIds);
            ObjectUtils.setFieldValue(obj, idName, oId);
        } else {
            Object value = getValue(fragment, s, ePhorteIds);
            ObjectUtils.setFieldValue(obj, name, value);
            if (value instanceof DataObjectT) {
                // this means a decorator made a DataObjectT instance
                // for us.  that instance will have to be included in
                // the list of objects we send to Gecko NCore
                return (DataObjectT) value;
            }
        }
        return null; // we didn't create a new DataObjectT instance
    }

    public Object getValue(Fragment fragment, Statement s, Map<String, Object> ePhorteIds) {
        if (decorators.containsKey(s.property)) {
            Decorator d = decorators.get(s.property);
            return d.process(fragment, s);
        }

        return s.object;
    }

    public Object getIdValue(DataObjectT obj, String fieldType, Statement s, Map<String, Object> ePhorteIds) {
        Object oId = ePhorteIds.get(s.object);

        if (oId == null) {
            DataObjectT o = get(fieldType, s.object);
            if (o == null) {
                String msg = String.format("Fragment <%s> tries to set property <%s> to non-existent object <%s>", s.subject, s.property, s.object);
                throw new ReferenceNotFound(msg, s);
            }

            oId = ObjectUtils.invokeGetter(o, "getId");
        }

        if (oId == null) {
            String msg = String.format("Fragment <%s> tries to set property <%s> to object <%s>, however we can't get the objects id", s.subject, s.property, s.object);
            throw new InvalidReference(msg);
        }

        String idName = getReferenceFieldName(s.property);
        if (!ObjectUtils.hasField(obj, idName)) {
            String msg = String.format("Fragment <%s> tries to set property <%s> to object <%s>, however subject has no setter <%s>", s.subject, s.property, s.object, idName);
            throw new InvalidReference(msg);
        }

        return oId;
    }

    public DataObjectT get(String typeName, String externalId) {
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
        String value = encodeExternalId(externalId);
        return externalIdSearchName + "=" + value;
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

    private boolean hasValue(Object obj, String name) {
        try {
            return ObjectUtils.getFieldValue(obj, name) != null;
        } catch (Exception e) {
            return true;
        }
    }

    private boolean valueIsMissing(Object obj, String name) {
        return !hasValue(obj, name);
    }

    private String getReferenceFieldName(String property) {
        String fieldName = getFieldName(property);
        return fieldName + "-id";
    }

    public String uploadFile(String fileName, byte[] data) {
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
