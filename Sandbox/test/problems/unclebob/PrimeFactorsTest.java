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

public class PrimeFactorsTest extends TestCase {
    
    // Right - BICEP
    // Boundary - Inverse - Cross-check - Error conditions - Performance within bounds?

    public void testOneHasNoPrimeFactors() {
        List<Integer> factors = new PrimeFactors().generate(1);
        assertEquals("generates an empty list of prime factors for 1", Collections.EMPTY_LIST, factors);
    }
    
	public void testGeneratesFactorsOf2() {
		List<Integer> factors = new PrimeFactors().generate(2);
        assertEquals("generates how many prime factors?", 1, factors.size());
        assertTrue("generates a prime factor, 2", factors.contains(2));
	}
	
	public void testGeneratesFactorsOf3() {
        List<Integer> factors = new PrimeFactors().generate(3);
        assertEquals("generates how many prime factors?", 1, factors.size());
        assertTrue("generates a prime factor, 3", factors.contains(3));
    }
    
	public void testGeneratesFactorsOf10() {
        List<Integer> factors = new PrimeFactors().generate(10);
        System.out.println("Factors computed: " + factors);
        assertEquals("generates how many prime factors?", 2, factors.size());
        assertTrue("generates a prime factor, 2", factors.contains(2));
        assertTrue("generates a prime factor, 5", factors.contains(5));
    }
 
	public void testFromFile() throws IOException {
	    String line;
	    
	    Integer n = null; // number to be factored
	    BufferedReader br = new BufferedReader(new FileReader("prime_factors_tests.txt"));
	    
	    while ( (line = br.readLine()) != null ) {
	        System.out.println("Line:" + line);
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
	        
	        assertEquals("generates correct number of prime factors", expectedFactors.size(), factors.size());
	        
	        Iterator<Integer> iter = expectedFactors.iterator();
	        while ( iter.hasNext() ) {
	            int i = iter.next();
	            assertTrue("generates a prime factor, " + i, factors.contains(i));
	        }
	    }
	    if ( n == null ) {
	        fail("no tests!");
	    }
	}
}
