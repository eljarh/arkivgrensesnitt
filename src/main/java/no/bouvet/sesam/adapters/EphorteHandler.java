package no.bouvet.sesam.adapters;

import no.priv.garshol.duke.StatementHandler;
import java.util.List;
import no.priv.garshol.duke.utils.ObjectUtils;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import java.util.ArrayList;

public class EphorteHandler implements StatementHandler {
    private static String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private List<Statement> literals = new ArrayList<Statement>();
    private List<Statement> resources = new ArrayList<Statement>();

    private DataObjectT obj;

    public void statement(String subject, String property, String object,
                          boolean literal) {
        if (rdfType.equals(property)) {
            String objType = RDFMapper.lookupObjectType(object);
            obj = (DataObjectT) ObjectUtils.instantiate(objType);
        } else if (literal) {
            literals.add(new Statement(subject, property, object));
        } else {
            resources.add(new Statement(subject, property, object));
        }
    }

    public DataObjectT[] getDataObjects() {
        return new DataObjectT[] { obj };
    }
}
