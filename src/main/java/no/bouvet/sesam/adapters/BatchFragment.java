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
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;

public class BatchFragment implements StatementHandler {
    private Map<String, Set<String>> dependencies;
    private Map<String, Fragment> fragments;

    public BatchFragment(Collection<String> resources, String source) throws InvalidFragment {
        fragments = new HashMap<String, Fragment>();
        dependencies = new HashMap<String, Set<String>>();

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
            throw new InvalidFragment("Couldn't parse fragment: " + e, e);
        }

        validate();
    }

    public void validate() throws InvalidFragment {
        for (Fragment f : fragments.values()) {
            f.validate();
        }
    }

    @Override
    public void statement(String subject, String predicate, String object, boolean literal) {
        Fragment f = fragments.get(subject);
        if (f != null) {
            f.statement(subject, predicate, object, literal);

            if (fragments.containsKey(object)) {
                addDependency(subject, object);
            }
        }
    }

    private void addDependency(String subject, String object) {
        Set<String> deps = dependencies.remove(subject);

        if (deps == null) {
            deps = new HashSet<String>();
        }
        deps.add(object);

        dependencies.put(subject, deps);
    }

    public List<Fragment> getFragments() throws InvalidFragment {
        // Make a copy so we don't change the fragments HashMap later
        Set<String> subjects = new HashSet<String>(fragments.keySet());

        List<Fragment> result = new ArrayList<Fragment>();
        while (subjects.size() > 0) {
            String subject = selectNextSubject(subjects);
            result.add(fragments.get(subject));
            subjects.remove(subject);
        }

        return result;
    }

    private String selectNextSubject(Set<String> candidates) throws InvalidFragment {
        for (String candidate : candidates) {
            int myScore = countDependencies(candidate, candidates);
            if (myScore == 0) return candidate;
        }

        throw new InvalidFragment("Fragment contains cycles");
    }

    private int countDependencies(String candidate, Collection<String> candidates) {
        Set<String> deps = dependencies.get(candidate);
        if (deps == null) return 0;

        Set<String> intersection = new HashSet<String>(candidates);
        intersection.retainAll(deps);

        return intersection.size();
    }
}
