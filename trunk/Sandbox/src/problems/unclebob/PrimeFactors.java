package problems.unclebob;

import java.util.ArrayList;
import java.util.List;

public class PrimeFactors {

    List<Integer> factors = new ArrayList<Integer>();
    
	public List<Integer> generate(int n) {
	    factors.add(1);
	    if ( n % 2 == 0 ) {
            factors.add(2);
        }
	    for ( int i = 3; i <= n / 2; i++) {
	        System.out.println("trying: " + i);
	        System.out.println("n % i = " + (n % i));
	        if ( n % i == 0 && isPrime(i) ) {
	            factors.add(i);
	        }
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
