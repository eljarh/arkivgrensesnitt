
package no.bouvet.sesam.adapters;

import org.junit.Test;
import static org.junit.Assert.*;

public class ZipCodeDecoratorTest {

    @Test
    public void testEmpty() {
        check("", "");
    }

    @Test
    public void testNorwegian() {
        check("1234", "1234");
    }

    @Test
    public void testExactlyFive() {
        check("12345", "12345");
    }

    @Test
    public void testAbitlong() {
        check("12345789", "12345");
    }

    @Test
    public void testLatvian() {
        check("LT-1007", "1007");
    }

    private void check(String input, String expected) {
        Statement s = new Statement("_", "_", input, false);
        Fragment fragment = new Fragment("_");
        ZipCodeDecorator decorator = new ZipCodeDecorator();
        String output = (String) decorator.process(fragment, s);
        assertEquals("wrong result from decorator", expected, output);
    }
}
