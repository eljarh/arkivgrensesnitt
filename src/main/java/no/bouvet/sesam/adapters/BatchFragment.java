package no.bouvet.sesam.adapters;

import no.priv.garshol.duke.StatementHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.io.Reader;
import java.io.StringReader;
import no.priv.garshol.duke.utils.NTriplesParser;
import java.io.IOException;
import java.util.ArrayList;

public class BatchFragment implements StatementHandler {
    private Map<String, Fragment> fragments;

    public BatchFragment(List<String> resources, String source) throws InvalidFragment {
        fragments = new HashMap<String, Fragment>();

        for (String resource: resources) {
            fragments.put(resource, new Fragment(resource));
        }

        parse(source);
    }

    private void parse(String source) throws InvalidFragment {
        Reader reader = new StringReader(source);
        try {
            NTriplesParser.parse(reader, this);
        } catch (IOException e) {
            throw new InvalidFragment("Couldn't parse fragment", e);
        }
    }

    @Override
    public void statement(String subject, String predicate, String object, boolean literal) {
        Fragment f = fragments.get(subject);
        if (f != null) {
            f.statement(subject, predicate, object, literal);
        }
    }

    public List<Fragment> getFragments() {
        return new ArrayList<Fragment>(fragments.values());
    }
}
