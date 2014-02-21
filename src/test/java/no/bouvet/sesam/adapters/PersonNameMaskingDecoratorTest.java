package no.bouvet.sesam.adapters;

import static org.junit.Assert.*;
import org.junit.Test;

public class PersonNameMaskingDecoratorTest {
    @Test
    public void testEmpty() {
        check("", "");
    }

    @Test
    public void testOneChar() {
        check("1", "1");
    }

    @Test
    public void testNormalTitle() {
        check("Utlysning fra WebCruiter", "Utlysning fra WebCruiter");
    }

    @Test
    public void testTitleWithName() {
        check("S\u00F8knad og CV - Test stilling #2 - st. ref. (2080564196) - @K\u00E5re Med H\u00E5ret", "S\u00F8knad og CV - Test stilling #2 - st. ref. (2080564196) - @#### ### #####");
    }

    // ----- UTILITIES

    private void check(String input, String output) {
        PersonNameMaskingDecorator dec = new PersonNameMaskingDecorator();
        Statement s = new Statement("1", "2", input, true);
        String out = (String) dec.process(null, s);
        assertEquals(output, out);
    }
}
