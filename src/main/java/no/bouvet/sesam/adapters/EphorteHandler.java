package no.bouvet.sesam.adapters;

import no.priv.garshol.duke.StatementHandler;
import java.util.List;
import no.priv.garshol.duke.utils.ObjectUtils;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import java.util.ArrayList;

public class EphorteHandler implements StatementHandler {
    private static String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private boolean created = false;
    private String mySubject = "";
    private String myType = "";

    private List<Statement> statements = new ArrayList<Statement>();
    
    public EphorteHandler(String s) {
        if (mySubject != null)
            mySubject = s;
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

    public DataObjectT[] getDataObjects() throws Exception {
        DataObjectT obj = EphorteFacade.get(myType, mySubject);

        if (obj == null) {
            created = true;
            obj = EphorteFacade.create(myType, mySubject);
        }

        EphorteFacade.populate(obj, statements);

        return new DataObjectT[] { obj };
    }
}
