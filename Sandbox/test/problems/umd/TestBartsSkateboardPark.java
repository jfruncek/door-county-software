package problems.umd;

import problems.umd._1.BartsSkateboardPark;
import java.util.HashMap;

import junit.framework.TestCase;

public class TestBartsSkateboardPark extends TestCase {

	HashMap<Integer, Integer> data1 = new HashMap<Integer, Integer>(); 
	
	@Override
	protected void setUp() throws Exception {
	}

	public void testFindLargestIndex() {
		data1.put(1, 1);
		assertEquals("block with largest number of jumps", 1, BartsSkateboardPark.getBlockWithLargestJumps(data1));
		data1.put(2, 5);
		data1.put(3, 2);
		assertEquals("block with largest number of jumps", 2, BartsSkateboardPark.getBlockWithLargestJumps(data1));
	}
}
