package no.bouvet.sesam.adapters;

import static org.junit.Assert.*;
import org.junit.Test;

import no.priv.garshol.duke.utils.NTriplesParser;
import no.gecko.ephorte.services.objectmodel.v3.en.dataobjects.CaseT;

public class EphorteHandlerTest {
    @Test
    public void testThatEphorteHandlerCanCreateCaseT(){
        EphorteHandler handler = new EphorteHandler("http://data.mattilsynet.org/cases/776663918");
        NTriplesParser parser = new NTriplesParser(handler);
        parser.parseLine("<http://data.mattilsynet.org/cases/776663918> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://data.mattilsynet.org/ontology/ePhorte/CaseT> .");
        parser.parseLine("<http://data.mattilsynet.org/cases/776663918> <http://data.mattilsynet.org/ontology/ePhorte/title> \"Seniorr\u00E5dgiver - plan og styring  - st. ref. 11/2010\" .");
        parser.parseLine("<http://data.mattilsynet.org/cases/776663918> <http://data.mattilsynet.org/ontology/ePhorte/series-id> \"SAK\" .");
        CaseT myCase = (CaseT) handler.getDataObjects()[0];
    }
}
