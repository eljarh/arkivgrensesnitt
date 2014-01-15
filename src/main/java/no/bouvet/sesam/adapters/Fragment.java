package no.bouvet.sesam.adapters;

import no.priv.garshol.duke.StatementHandler;
import no.priv.garshol.duke.utils.NTriplesParser;

import java.util.List;
import no.priv.garshol.duke.utils.ObjectUtils;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Reader;
import java.io.StringReader;

public class Fragment implements StatementHandler {
    private static String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    static Logger log = LoggerFactory.getLogger(Fragment.class.getName());

    private boolean created = false;
    private String resourceId = "";
    private String source = "";
    private String type = "";

    private List<Statement> statements = new ArrayList<Statement>();
    
    public Fragment(String resourceId, String source) {
        if (resourceId != null)
            this.resourceId = resourceId;
        this.source = source;
    }

    public boolean shouldUpdate() {
        return !created;
    }

    public void statement(String subject, String property, String object,
                          boolean literal) {
        if (rdfType.equals(property)) {
            type = RDFMapper.getObjectType(object);
        }

        statements.add(new Statement(subject, property, object, literal));
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getSource() {
        return source;
    }

    public DataObjectT[] getDataObjects(EphorteFacade facade) throws Exception {
        Reader reader = new StringReader(source);
        NTriplesParser.parse(reader, this);

        if (StringUtils.isBlank(type)) {
            throw new RuntimeException("Fragment has no type");
        }

        if (StringUtils.isBlank(resourceId)) {
           throw new RuntimeException("Fragment has no resourceId");
        }

        log.debug("Looking up object with type {} and resourceId {}", type, resourceId);
        DataObjectT obj = facade.get(type, resourceId);

        if (obj == null) {
            log.debug("Creating object with type {} and resourceId {}", type, resourceId);
            obj = facade.create(type, resourceId);
            created = true;
        }

         facade.populate(obj, statements);

        return new DataObjectT[] { obj };
    }
}
