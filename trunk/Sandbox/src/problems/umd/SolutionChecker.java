/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package problems.umd;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author jfruncek
 */
public class SolutionChecker {
  public static final String PATH = "src/problems/umd";

  public static void main(String[] args) throws IOException {

    File path = new File(PATH);
    File[] possibleSolutionPaths = path.listFiles();

    for (File solutionPath : possibleSolutionPaths) {
      System.out.println("Found possible solution path: " + solutionPath.getPath());
      System.out.println("name: " + solutionPath.getName());
      System.out.println("absolute path: " + solutionPath.getAbsolutePath());
      System.out.println("canonical path: " + solutionPath.getCanonicalPath());
      System.out.println("exists(): " + solutionPath.exists());
      System.out.println("isAbsolute(): " + solutionPath.isAbsolute());
      System.out.println("isFile(): " + solutionPath.isFile());
      System.out.println("isHidden(): " + solutionPath.isHidden());
      System.out.println("isDirectory(): " + solutionPath.isDirectory());
      if ( isSolutionPath(solutionPath) ) {
        checkSolution(solutionPath);
      }
    }
  }

  private static void checkSolution(File solutionPath) {
    System.out.println("Found solution path: " + solutionPath);
  }

  protected static boolean isSolutionPath(File file) {
    return file.isDirectory() && ! file.isHidden();
  }
}
