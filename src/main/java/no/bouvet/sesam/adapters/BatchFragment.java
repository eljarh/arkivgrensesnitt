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
            throw new InvalidFragment("Couldn't parse fragment", e);
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

    public List<Fragment> getFragments() {
        List<String> subjects = new ArrayList<String>(fragments.keySet());
        Collections.sort(subjects, new Comparator<String>() {

                public int compare(String s1, String s2) {
                    int i1 = countDependencies(s1);
                    int i2 = countDependencies(s2);

                    // The number of dependencies should never be able
                    // to cause an integer overflow.
                    return i1 - i2;
                }
            });

        ArrayList<Fragment> result = new ArrayList<Fragment>();
        while (subjects.size() > 0) {
            String subject = selectNextSubject(subjects);
            result.add(fragments.get(subject));
            subjects.remove(subject);
        }

        return result;
    }

    private int countDependencies(String subject) {
        Set<String> deps = dependencies.get(subject);
        if (deps == null) return 0;
        return deps.size();
    }

    private String selectNextSubject(List<String> candidates) {
        String best = candidates.get(0);
        int score = 1000;

        for (String candidate : candidates) {
            int myScore = 0;

            Set<String> deps = dependencies.get(candidate);
            if (deps != null) {
                myScore = deps.size();
            }

            if (myScore < score && !dependsOnOtherCandidate(candidate, candidates)) {
                best = candidate;
                score = myScore;
            }
        }

        return best;
    }

    private boolean dependsOnOtherCandidate(String candidate, List<String> candidates) {
        Set<String> deps = dependencies.get(candidate);
        if (deps == null) return false;

        Set<String> intersection = new HashSet<String>(candidates);
        intersection.retainAll(deps);
        return intersection.size() > 0;
    }
}
