package tupletest;

import junit.framework.*;
import tuplespaces.*;
import concassess.testee.*;

/**
 * This test performs further checks on the tuple space's pattern matching and blocking semantics. 
 */
public class PatternMatchingTest extends TestCase {
	TupleSpace t;

	public void setUp() {
		t = new LocalTupleSpace();
	}

	public void testPatternMatchingTest() {
		String[] b = new String[] { "Hello", "World" };
		t.put(b);
		t.put(new String[] { "Hello", "Warld" });
		t.put(new String[] { "Hello", "Wårld" });
		t.put(new String[] { "10", "true" });
		t.put(new String[] { "5", "true" });
		t.put(new String[] { "17", "false" });
		String[] a = new String[] { "Hello", "World", "Again" };
		t.put(a);
		t.put(new String[] { "Hello", "World" });
		t.put(new String[] { "pik" });
		String[] w = t.read(null, "World");
		assertEquals("Matching test tuple length", 2, w.length);
		assertEquals("Matching test tuple[0]", "Hello", w[0]);
		assertEquals("Matching test tuple[1]", "World", w[1]);

		assertEquals("Got unexpected length tuple", t.get("pik").length, 1);
		w = t.get(null, "true");
		String[] w2 = t.get(null, "true");
		assertEquals("Matching test tuple length", 2, w.length);
		assertEquals("Matching test tuple length", 2, w2.length);
		assertEquals("Matching test tuple numbers", 15,
				(new Integer(w[0]).intValue())
						+ (new Integer(w2[0]).intValue()));
	}

	public static void main(String[] args) {
		new ConcTestRunner(args).start(PatternMatchingTest.class);
	}
}
