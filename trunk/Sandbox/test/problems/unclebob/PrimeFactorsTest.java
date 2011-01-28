package problems.unclebob;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.TestCase;

/*
 * See http://katas.softwarecraftsmanship.org/?p=139
 */
public class PrimeFactorsTest extends TestCase {
    
    private static final String GENERATES_A_PRIME_FACTOR = " generates a prime factor, ";
    private static final String GENERATES_HOW_MANY_PRIME_FACTORS = "Generates how many prime factors?";

        // Right - BICEP
    // Boundary - Inverse - Cross-check - Error conditions - Performance within bounds?

    public void testOneHasNoPrimeFactors() {
        List<Integer> factors = new PrimeFactors().generate(1);
        assertEquals("generates an empty list of prime factors for 1", Collections.EMPTY_LIST, factors);
    }
    
	public void testGeneratesFactorsOf2() {
		List<Integer> factors = new PrimeFactors().generate(2);
        assertEquals(GENERATES_HOW_MANY_PRIME_FACTORS, 1, factors.size());
        assertTrue(GENERATES_A_PRIME_FACTOR + 2, factors.contains(2));
	}
	
    public void testGeneratesFactorsOf3() {
        List<Integer> factors = new PrimeFactors().generate(3);
        assertEquals(GENERATES_HOW_MANY_PRIME_FACTORS, 1, factors.size());
        assertTrue(GENERATES_A_PRIME_FACTOR + 3, factors.contains(3));
    }
    
    public void testGeneratesFactorsOf4() {
        List<Integer> factors = new PrimeFactors().generate(4);
        assertEquals(GENERATES_HOW_MANY_PRIME_FACTORS, 2, factors.size());
        assertEquals("generates 2 prime factors: 2", 2, countOf(2, factors));
    }
    
    /**
     * @param f a number
     * @return count of appearances of the number in a list
     */
    private int countOf(int f, List<Integer> list) {
        int count = 0;
        for (Integer integer : list) {
            if (integer == f) count++;
        }
        return count;
    }
    
	public void testFromFile() throws IOException {
	    String line;
	    
	    Integer n = null; // number to be factored
	    BufferedReader br = new BufferedReader(new FileReader("prime_factors_tests.txt"));
	    
	    while ( (line = br.readLine()) != null ) {
	        //System.out.println("Line:" + line);
	        if (line.startsWith("#")) { //comment line
	            continue;
	        }
	        StringTokenizer st = new StringTokenizer(line);
	        while ( !st.hasMoreElements() ) { //blank line
	            continue;
	        }
	        // get number to be factored first
	        String token = st.nextToken();
	        n = Integer.parseInt(token);
	        // read expected factors
	        List<Integer> expectedFactors = new ArrayList<Integer>();
	        while ( st.hasMoreElements()) {
	            expectedFactors.add( Integer.parseInt(st.nextToken()) );
	        }
	        
	        List<Integer> factors = new PrimeFactors().generate(n);
	        
	        assertEquals("For " + n + GENERATES_HOW_MANY_PRIME_FACTORS, expectedFactors.size(), factors.size());
	        
	        Iterator<Integer> iter = expectedFactors.iterator();
	        while ( iter.hasNext() ) {
	            int i = iter.next();
	            assertTrue("For " + n + GENERATES_A_PRIME_FACTOR + i, factors.contains(i));
	        }
	    }
	    if ( n == null ) {
	        fail("no tests!");
	    }
	}
}
