package problems.stanford;

import java.util.*;
import java.text.DecimalFormat;

public class Sample {
	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		DecimalFormat fmt = new DecimalFormat("0.00");
		while (true) {
			int n = s.nextInt();
			if (n == -999)
				break;
			if (n > 0)
				System.out.println(fmt.format((double) (n * (n + 1) / 2) / n));
			else
				System.out.println(fmt.format((double) (1 + n * (1 - n) / 2)
						/ (2 - n)));
		}
	}
}
