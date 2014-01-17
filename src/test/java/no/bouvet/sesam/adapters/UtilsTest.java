package no.bouvet.sesam.adapters;

import static org.junit.Assert.*;
import org.junit.Test;

public class UtilsTest {
    @Test
    public void testGetFirstSubject() {
        String subject = Utils.getFirstSubject("<id> blubber");
        assertEquals("id", subject);
    }
}
