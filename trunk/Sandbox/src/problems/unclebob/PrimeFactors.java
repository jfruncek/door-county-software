package problems.unclebob;

import java.util.ArrayList;
import java.util.List;

public class PrimeFactors {

    static List<Integer> factors = new ArrayList<Integer>();
    
	public static List<Integer> generate(int i) {
	    factors.add(1);
	    if ( isPrime(i) ) {
	        factors.add(i);
	    }
		return factors;
	}

    private static boolean isPrime(int i) {
        if ( i == 2 ) { return true; };
        return false;
    }

}
