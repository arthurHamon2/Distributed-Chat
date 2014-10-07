package tupletest;

import junit.framework.*;
import tuplespaces.*;
import concassess.testee.*;

/**
 * This test checks the tuple space's pattern matching and blocking semantics. 
 */
public class BasicTupleTest extends TestCase {
	TupleSpace t;

	public void setUp() {
		t = new LocalTupleSpace();
	}

	public void testBasicTupleTest() {
		String[] b = new String[] { "Hello", "World" };
		String[] c = b.clone();
		t.put(c);
		c[1] = "Again";
		String[] r = t.read("Hello", null);
		assertNotNull("read returned null", r);
		assertEquals("read returns tuple of different length than pattern",
				b.length, r.length);
		assertFalse(
				"Tuple returned by read affected by change made after it was put",
				c[1].equals(r[1]));
		assertEquals("read returns tuple other than the tuple that was put",
				b[0], r[0]);
		assertEquals("read returns tuple other than the tuple that was put",
				b[1], r[1]);
		r = t.get("Hello", null);
		assertNotNull("get returned null", r);
		assertEquals("get returns tuple of different length than pattern",
				b.length, r.length);
		assertFalse(
				"Tuple returned by get affected by change made after it was put",
				c[1].equals(r[1]));
		assertEquals("get returns tuple other than the tuple that was put",
				b[0], r[0]);
		assertEquals("get returns tuple other than the tuple that was put",
				b[1], r[1]);
	}

	public static void main(String[] args) {
		new ConcTestRunner(args).start(BasicTupleTest.class);
	}
}
