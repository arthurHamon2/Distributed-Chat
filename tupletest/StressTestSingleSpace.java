package tupletest;

import junit.framework.TestCase;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import tuplespaces.*;
import concassess.testee.*;

/**
 * This test transfers large amounts of tuples between a large amount of threads
 * putting tuples in the space in an attempt to cause concurrency-related
 * defects to manifest as failures such as missing tuples.
 */
public class StressTestSingleSpace extends TestCase {
	private TupleSpace t;
	private int p1max, p2max;
	private int p1res, p2res;
	public static final int STRESS_SIZE = 30, STRESS_LENGTH = 10000;
	static ConcTestRunner ctr;
	private static final String WRONG_LENGTH = "get returns tuple of different length than pattern";
	private static final String WRONG_CONTENT = "get returned tuple with incorrect contents";
	private static final String WRONG_TUPLE = "get returned tuple that does not match pattern";
	private static final String TWICE = "get returned the same tuple twice; get should remove the tuple it returns";

	protected void setUp() {
		t = new LocalTupleSpace();
	}

	protected void tearDown() {
		t = null;
	}

	String[] pattern1 = { "Hello", "Test", null, null };
	String[] pattern2 = { "Hello", "Wurld", "agann", null };
	String[] pattern3 = { "Hello", "World", "Again", null };

	String[] base1 = { "Hello", "Test", "Again", "!" };
	String[] base2 = { "Hello", "Wurld", "agann", "!" };
	String[] base3 = { "Hello", "World", "Again", "!" };

	public void testStressTestSingleSpace() throws Throwable {
		TestRunnable tct[] = new TestRunnable[STRESS_SIZE * 2 + 2];
		for (int i = 0; i < STRESS_SIZE * 2; i++)
			tct[i] = new TestRunnable() {
				public void runTest() {
					String[] w = t.get(pattern1);
					assertEquals(WRONG_LENGTH, 4, w.length);
					assertEquals(WRONG_CONTENT, "Hello", w[0]);
					assertEquals(WRONG_TUPLE, "Test", w[1]);
					assertEquals(WRONG_CONTENT, "Again", w[2]);
				}
			};

		tct[STRESS_SIZE * 2] = new TestRunnable() {
			public void runTest() {
				boolean got[] = new boolean[STRESS_LENGTH];
				for (int i = 0; i < STRESS_LENGTH; i++) {
					String[] p = base3.clone();
					p[3] = "" + p1max;
					p1max++;
					t.put(p);
					String[] w = t.get(pattern2);
					assertEquals(WRONG_LENGTH, 4, w.length);
					assertEquals(WRONG_CONTENT, "Hello", w[0]);
					assertEquals(WRONG_TUPLE, "Wurld", w[1]);
					assertEquals(WRONG_CONTENT, "agann", w[2]);
					assertTrue("get returned too many tuples: " + p1max
							+ " expected, got " + p2max, (p1max >= p2max - 1));
					int n = new Integer(w[3]);
					assertFalse(TWICE, got[n]);
					got[n] = true;
					assertFalse("get skipped a tuple", n > p1res + 1);
					p1res++;
					if (p1res % 100 == 0 && ctr != null)
						ctr.resetWatchdog();
				}
				for (int i = 0; i < STRESS_SIZE; i++)
					t.put(base1);
			}
		};

		tct[STRESS_SIZE * 2 + 1] = new TestRunnable() {
			public void runTest() {
				boolean got[] = new boolean[STRESS_LENGTH];
				for (int i = 0; i < STRESS_LENGTH; i++) {
					String[] p = base2.clone();
					p[3] = "" + p2max;
					p2max++;
					t.put(p);
					String[] w = t.read(pattern3);
					assertEquals(WRONG_LENGTH, 4, w.length);
					assertEquals(WRONG_CONTENT, "Hello", w[0]);
					assertEquals(WRONG_TUPLE, "World", w[1]);
					assertEquals(WRONG_CONTENT, "Again", w[2]);
					assertTrue("read returned too many tuples: " + p2max
							+ " expected, got " + p1max, (p2max >= p1max - 1));

					w = t.get(pattern3);
					assertEquals(WRONG_LENGTH, 4, w.length);
					assertEquals(WRONG_CONTENT, "Hello", w[0]);
					assertEquals(WRONG_TUPLE, "World", w[1]);
					assertEquals(WRONG_CONTENT, "Again", w[2]);
					assertTrue("get returned too many tuples: " + p2max
							+ " expected, got " + p1max, (p2max >= p1max - 1));
					int n = new Integer(w[3]);
					assertFalse(TWICE, got[n]);
					got[n] = true;
					assertFalse("get skipped a tuple", n > p2res + 1);
					p2res++;
				}
				for (int i = 0; i < STRESS_SIZE; i++)
					t.put(base1);
			}
		};
		MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(tct);
		mttr.runTestRunnables();
	}

	public static void main(String args[]) {
		ctr = new ConcTestRunner(args);
		ctr.start(StressTestSingleSpace.class);
	}
}
