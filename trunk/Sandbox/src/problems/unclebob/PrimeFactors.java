package problems.unclebob;

import java.util.ArrayList;
import java.util.List;

/*
 * See http://en.wikipedia.org/wiki/Prime_factor
 * 
 * Only positive numbers have prime factors; the number 1 has no prime factors.
 */
public class PrimeFactors {

    List<Integer> factors = new ArrayList<Integer>();
    
	public List<Integer> generate(int n) {
        if ( n % 2 == 0) {
            factors.add(2);
            n /= 2;
        }
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
