package problems.unclebob;

import java.util.List;

import junit.framework.TestCase;

public class PrimeFactorsTest extends TestCase {

	public void testGeneratesFactorsOf2() {
		List<Integer> factors = PrimeFactors.generate(2);
        assertEquals("generates how many prime factors?", 2, factors.size());
		assertTrue("generates a prime factor, 1", factors.contains(1));
        assertTrue("generates a prime factor, 2", factors.contains(2));
	}
	
	public void testGeneratesFactorsOf10() {
        List<Integer> factors = PrimeFactors.generate(10);
        assertEquals("generates how many prime factors?", 3, factors.size());
        assertTrue("generates a prime factor, 1", factors.contains(1));
        assertTrue("generates a prime factor, 2", factors.contains(2));
        assertTrue("generates a prime factor, 5", factors.contains(5));
    }
    
}
