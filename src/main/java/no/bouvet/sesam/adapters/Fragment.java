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
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import no.priv.garshol.duke.utils.NTriplesWriter;

public class Fragment implements StatementHandler {
    private static String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    static Logger log = LoggerFactory.getLogger(Fragment.class.getName());

    private String type;
    private String resourceId;
    private String source = null;
    private List<Statement> statements = null;
    private DataObjectT object;

    public Fragment(String resourceId) {
        this.resourceId = resourceId;
        statements = new ArrayList<Statement>();
    }
    
    public Fragment(String resourceId, String source) throws InvalidFragment {
        this.resourceId = resourceId;
        this.source = source;

        statements = new ArrayList<Statement>();

        parse();
        validate();
    }

    public void statement(String subject, String property, String object,
                          boolean literal) {
        if (rdfType.equals(property)) {
            type = object;
        }
        statements.add(new Statement(subject, property, object, literal));
    }

    public String getType() {
        return type;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getSource() {
        if (source == null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NTriplesWriter writer = new NTriplesWriter(out);
            for (Statement s : statements) {
                writer.statement(s.subject, s.property, s.object, s.literal);
            }
            try {
                writer.done ();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            source = out.toString();
        }

        return source;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public Statement getStatementWithSuffix(String suffix) {
        for (Statement s : statements)
            if (s.property.endsWith(suffix))
                return s;
        return null;
    }

    public void setDataObject(DataObjectT object) {
        this.object = object;
    }

    public DataObjectT getDataObject() {
        return object;
    }
    
    private void parse() throws InvalidFragment {
        Reader reader = new StringReader(source);
        try {
            NTriplesParser.parse(reader, this);
        } catch (Exception e) {
            throw new InvalidFragment("Couldn't parse fragment", e);
        }
    }

    public void validate() {
        if (StringUtils.isBlank(this.resourceId)) {
            throw new InvalidFragment("Fragment has no identity");
        }

        if (StringUtils.isBlank(this.type)) {
            throw new InvalidFragment("Fragment has no type: " + resourceId);
        }
    }
}
