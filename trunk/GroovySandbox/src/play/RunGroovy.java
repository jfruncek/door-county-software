/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package play;

/**
 *
 * @author jfruncek
 */
public class RunGroovy {

  public static void main(String[] args) {
      Greet.main(new String[] {"johnny main"});

      Greet greet = new Greet("johnny salute");
      greet.salute();
      
      CoolGreet.main(new String[] {"johnny cool"});
  }
}
