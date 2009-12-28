package problems.stanford;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Scanner;

public class Bonsai {

	public static void main(String[] args) {
		Scanner scanner = null;
		try {
			File source = new File("src/problems/stanford/bonsai.in");
			System.out.println(source.getAbsolutePath());
			scanner = new Scanner(source);
		} catch (FileNotFoundException e1) {
			System.out.println("Cannot find input file");
		}
		DecimalFormat format = new DecimalFormat("0");
		
		int numVertices = scanner.nextInt();
		int root = scanner.nextInt();
		System.out.println(numVertices + " " + root);
		
		while (true) {
			int u = scanner.nextInt();
			int v = scanner.nextInt();
			if ( u == 0 && v == 0) break;
			int w = scanner.nextInt();
			System.out.println(u + " " + v + " " + w);
		}
	}

}
