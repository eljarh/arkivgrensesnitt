package no.bouvet.sesam.adapters;

import no.priv.garshol.duke.StatementHandler;
import java.util.List;
import no.priv.garshol.duke.utils.ObjectUtils;
import no.gecko.ephorte.services.objectmodel.v3.en.DataObjectT;
import java.util.ArrayList;

public class EphorteHandler implements StatementHandler {
    private static String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private boolean shouldCreate = true;
    private String mySubject = "";
    private List<Statement> literals = new ArrayList<Statement>();
    private List<Statement> resources = new ArrayList<Statement>();
    
    private DataObjectT obj;

    public EphorteHandler(String s) {
        if (mySubject != null)
            mySubject = s;
    }

    public boolean shouldUpdate() {
        return !shouldCreate;
    }

    public void statement(String subject, String property, String object,
                          boolean literal) {
        if (rdfType.equals(property)) {
            String typeName = RDFMapper.getObjectType(object);
            try {
                obj = EphorteFacade.getByExternalId(typeName, mySubject);
                shouldCreate = (obj == null);
            } catch (Exception e) {
                /* FIXME: Shit, we have duplicates.  This must be fixed. */
            }

            if (shouldCreate) {
                obj = (DataObjectT) ObjectUtils.instantiate(typeName);
                EphorteFacade.setExternalId(obj, mySubject);
            }
        } else if (literal) {
            literals.add(new Statement(subject, property, object));
        } else {
            resources.add(new Statement(subject, property, object));
        }
    }

    public DataObjectT[] getDataObjects() {
        addLiterals(obj, literals);
        addResources(obj, resources);

        return new DataObjectT[] { obj };
    }

    protected void addLiterals(DataObjectT obj, List<Statement> statements) {
        for (Statement s : statements) {
            String name = RDFMapper.getFieldName(s.property);
            ObjectUtils.setBeanProperty(obj, name, s.object, null);
        }
    }

    protected void addResources(DataObjectT obj, List<Statement> statements) {
        for (Statement s : statements) {
            String name = RDFMapper.getFieldName(s.property);
            String value = RDFMapper.getResourceId(s.object);
            ObjectUtils.setBeanProperty(obj, name, value, null);
        }
    }
}
