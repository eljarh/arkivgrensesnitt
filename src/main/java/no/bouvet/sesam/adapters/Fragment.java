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

public class Fragment implements StatementHandler {
    private static String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    static Logger log = LoggerFactory.getLogger(Fragment.class.getName());

    private String type;
    private String resourceId;
    private String source;
    private List<Statement> statements = null;
    
    public Fragment(String resourceId, String source) throws InvalidFragment {
        this.resourceId = resourceId;
        this.source = source;

        try {
            parse();
        } catch (RuntimeException e) {
            throw new InvalidFragment(e, "Couldn't parse fragment");
        }

        if (StringUtils.isBlank(this.resourceId)) {
            throw new InvalidFragment("Fragment has no identity");
        }

        if (StringUtils.isBlank(this.type)) {
            throw new InvalidFragment("Fragment has no type");
        }
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
        return source;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    private void parse() {
        statements = new ArrayList<Statement>();

        Reader reader = new StringReader(source);
        try {
            NTriplesParser.parse(reader, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
