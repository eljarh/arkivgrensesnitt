package no.bouvet.sesam.adapters;

import static org.junit.Assert.*;
import org.junit.Test;

public class StatementTest {
    @Test
    public void testThatEqualStatementsAreEqual() {
        Statement a = new Statement("1", "2", "3", true);
        Statement b = new Statement("1", "2", "3", true);

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
    }

    @Test
    public void testThatNonEqualStatementsAreNotEqual() {
        Statement a = new Statement("1", "2", "3", true);
        Statement b = new Statement("1", "2", "3", false);

        assertTrue(!a.equals(b));
        assertTrue(!b.equals(a));
    }

    @Test
    public void testThatStatementEqualsCanHandleNull() {
        Statement a = new Statement("1", "2", "3", true);
        Statement b = null;

        assertTrue(!a.equals(b));
    }

    @Test
    public void testThatStatementEqualsRecognizesSame() {
        Statement a = new Statement("1", "2", "3", true);
        Statement b = a;

        assertTrue(a.equals(b));
    }

    @Test
    public void testThatStatementEqualsCanHandleDifferentObjects() {
        Statement a = new Statement("1", "2", "3", true);
        String b = "i-am-not-equal";

        assertTrue(!a.equals(b));
        assertTrue(!b.equals(a));
    }

    @Test
    public void testThatNonEqualStatementsHaveDifferentHash() {
        Statement a = new Statement("1", "2", "3", true);
        Statement b = new Statement("1", "2", "3", false);

        assertTrue(a.hashCode() != b.hashCode());
    }

    @Test
    public void testThatEqualStatementsHaveSameHash() {
        Statement a = new Statement("1", "2", "3", true);
        Statement b = new Statement("1", "2", "3", true);
        
        assertTrue(a.hashCode() == b.hashCode());
    }
}
