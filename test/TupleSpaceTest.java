package test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import tuplespaces.LocalTupleSpace;
import tuplespaces.TupleSpace;

public class TupleSpaceTest {

	TupleSpace tupleSpace;
	
	@Before
	public void setup(){
		tupleSpace = new LocalTupleSpace();
	}
	
	@Test
	public void test() {
		tupleSpace.put("1");
		tupleSpace.put("2");
		assertTrue(tupleSpace.read("1")[0] == "1") ;
		assertTrue(tupleSpace.read("2")[0] == "2") ;
	}

}
