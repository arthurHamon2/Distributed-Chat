package concassess.testee;

import junit.framework.TestCase;

/**
 * Stub test runner to allow concassess framework tests to run without the framework.
 * This test runner behaves like the normal textual runner in JUnit 3.
 *  
 * @author Jan Lönnberg
 *
 */
public class ConcTestRunner extends junit.textui.TestRunner {
	public ConcTestRunner(String[] args) {
		super();
	}
	
	public void start(Class<? extends TestCase> c) {
		run(c);
	}

	public void resetWatchdog() {
		// Watchdog timer reset is not supported in JUnit itself.
	}

	public void sendDiag(String string) {
		System.err.println(string);
	}
}
