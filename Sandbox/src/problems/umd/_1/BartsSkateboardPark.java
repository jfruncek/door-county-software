package problems.umd._1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class BartsSkateboardPark {
    public static final String PATH = "src/problems/umd/_1/";
    private static Map<Integer, Integer> blocks = new HashMap<Integer, Integer>();
    private static Set<Integer> largest = new TreeSet<Integer>();

    public static void main(String[] args) throws FileNotFoundException, IOException {

        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setMinimumIntegerDigits(2);

        for (int filenum = 1; filenum <= 10; filenum++) {
            final String filename = nf.format(filenum);
            File file = new File(PATH + filename + ".in");
            Scanner scanner = new Scanner(file);

            // read blocks
            while (scanner.hasNextInt()) {
                int block = scanner.nextInt();
                int jumps = scanner.nextInt();
                blocks.put(block,jumps);
                System.out.println("Read block: " + block + " jumps: " + jumps);
            }

            // find 3 blocks with largest number of jumps
            for (int i = 0; i < 3; i++) {
                int largestBlock = getBlockWithLargestJumps(blocks);
                System.out.println("Block with most jumps: " + largestBlock + " ");
                largest.add(largestBlock);
                blocks.remove(largestBlock);
            }

            // output answer
            file = new File(PATH + filename + ".out");
            Writer w = new PrintWriter(file);
            for (Iterator<Integer> it = largest.iterator(); it.hasNext();) {
                Integer integer = it.next();
                w.write(integer);
                if (it.hasNext()) {
                  w.write(" ");
                }
            }
            w.close();
        }
    }

    public static int getBlockWithLargestJumps(Map<Integer, Integer> map) {
        int largest = 0;
        int answer = 0;
        for (Integer block : map.keySet()) {
            if (map.get(block) > largest) {
                largest = map.get(block);
                answer = block;
            }
        }
        return answer;
    }
}
