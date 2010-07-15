package problems.unclebob;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class PrimeFactorsTest extends TestCase {

	public void testGenerates() {
		List<Integer> factors = PrimeFactors.generate(10);
		assertTrue("generates prime factors", Arrays.equals(new Integer[] {1, 2, 5}, factors.toArray(new Integer[] {})));
	}

}
