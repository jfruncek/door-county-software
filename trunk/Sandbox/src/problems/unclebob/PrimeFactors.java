package problems.unclebob;

import java.util.ArrayList;
import java.util.List;

public class PrimeFactors {

    List<Integer> factors = new ArrayList<Integer>();
    
	public List<Integer> generate(int n) {
	    if ( n > 1 ) {
            factors.add(n);
        }
		return factors;
	}

    private static boolean isPrime(int n) {
        if ( n == 1 || n == 2 ) { return true; };
        for ( int i = 3; i < n / 2; i++) {
            if ( n % i == 0 ) {
                return false;
            }
        }
        return true;
    }

}
