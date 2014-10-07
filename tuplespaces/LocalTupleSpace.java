package tuplespaces;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class LocalTupleSpace implements TupleSpace {

	private Map<Integer, LinkedList<String[]>> bag;

	public LocalTupleSpace() {
		bag = new HashMap<Integer, LinkedList<String[]>>();
	}

	public String[] get(String... pattern) {
		LinkedList<String[]> monitor;
		int tupleIndex;
		synchronized (bag) {
			if (!bag.containsKey(pattern.length)) {
				bag.put(pattern.length, new LinkedList<String[]>());
			}
			monitor = bag.get(pattern.length);
		}
		synchronized (monitor) {
			while ((tupleIndex = isPresent(pattern)) == -1) {
				try {
					monitor.wait();
				} catch (InterruptedException e) {}
			}
		return monitor.remove(tupleIndex);
		}

	}

	public String[] read(String... pattern) {
		LinkedList<String[]> monitor;
		int tupleIndex;
		synchronized (bag) {
			if (!bag.containsKey(pattern.length)) {
				bag.put(pattern.length, new LinkedList<String[]>());
			}
			monitor = bag.get(pattern.length);
		}
		synchronized (monitor) {
			while ((tupleIndex = isPresent(pattern)) == -1) {
				try {
					monitor.wait();
				} catch (InterruptedException e) {}
			}
			return monitor.get(tupleIndex).clone();
		}
	}

	public void put(String... tuple) {
		LinkedList<String[]> list;
		synchronized (bag) {
			if (!bag.containsKey(tuple.length)) {
				bag.put(tuple.length, new LinkedList<String[]>());
			}
			list = bag.get(tuple.length);
		}
		synchronized (list) {
			list.addFirst(tuple.clone());
			list.notifyAll();
		}
	}

	/**
	 * Return the index which corresponds to the pattern in parameter.
	 * If the pattern is not found, we return an index of -1.
	 * @param pattern the pattern to look for
	 * @return the index in the list of the matching pattern
	 */
	private int isPresent(String... pattern) {
		boolean patternFound;
		if (bag.containsKey(pattern.length)) {
			int index = 0;
			for (String[] tuple : bag.get(pattern.length)) {
				patternFound = true;
				int patternIndex = 0;
				int tupleIndex = 0;
				
				while (tupleIndex < tuple.length && patternFound) {
					if (pattern[patternIndex] != null){
						if (pattern[patternIndex].hashCode() != tuple[tupleIndex]
								.hashCode()) {
							// Leave the loop
							patternFound = false;
						}
					}
					patternIndex++;
					tupleIndex++;
				}
				// If the boolean stayed at true, then we found the pattern
				if (patternFound) {
					return index;
				}
				index++;
			}
		}
		return -1;
	}
}
