package no.bouvet.sesam.adapters;

import no.priv.garshol.duke.StatementHandler;
import java.util.List;
import no.priv.garshol.duke.utils.ObjectUtils;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;

public class EphorteHandler implements StatementHandler {
    private static String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private List<Statement> statements;
    private DataObjectT obj;

    public void statement(String subject, String property, String object,
                          boolean literal) {
        if (rdfType.equals(property)) {
            String objType = RDFMapper.lookupObjectType(property);
            obj = (DataObjectT) ObjectUtils.instantiate(objType);
        }
        
        statements.add(new Statement(subject, property, object, literal));
    }

    public DataObjectT[] getDataObjects() {
        return new DataObjectT[] { obj };
    }
}
