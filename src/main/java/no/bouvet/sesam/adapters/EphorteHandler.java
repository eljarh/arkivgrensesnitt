package no.bouvet.sesam.adapters;

import no.priv.garshol.duke.StatementHandler;
import java.util.List;
import no.priv.garshol.duke.utils.ObjectUtils;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EphorteHandler implements StatementHandler {
    private static String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    static Logger log = LoggerFactory.getLogger(EphorteHandler.class.getName());

    private boolean created = false;
    private String myResource = "";
    private String myType = "";

    private List<Statement> statements = new ArrayList<Statement>();
    
    public EphorteHandler(String s) {
        if (myResource != null)
            myResource = s;
    }

    public boolean shouldUpdate() {
        return !created;
    }

    public void statement(String subject, String property, String object,
                          boolean literal) {
        if (rdfType.equals(property)) {
            myType = RDFMapper.getObjectType(object);
        }

        statements.add(new Statement(subject, property, object, literal));
    }

    public String getResourceId() {
        return myResource;
    }

    public DataObjectT[] getDataObjects() throws Exception {
        if (StringUtils.isBlank(myType)) {
            throw new RuntimeException("Fragment has no type");
        }

        if (StringUtils.isBlank(myResource)) {
           throw new RuntimeException("Fragment has no resource");
        }

        log.debug("Looking up object with type {} and resourceId {}", myType, myResource);
        DataObjectT obj = EphorteFacade.get(myType, myResource);

        if (obj == null) {
            log.debug("Creating object with type {} and resourceId {}", myType, myResource);
            obj = EphorteFacade.create(myType, myResource);
            created = true;
        }

         EphorteFacade.populate(obj, statements);

        return new DataObjectT[] { obj };
    }
}
