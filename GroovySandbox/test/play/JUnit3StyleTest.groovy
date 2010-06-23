package play;

import groovy.util.GroovyTestCase

class JUnit3StyleTest extends GroovyTestCase {
  
  public void testSomething() {
        assert 1 == 1
        assert 2 + 2 == 4 : "We're in trouble, arithmetic is broken"
    }

  public void testGreet() {
		def greet = new Greet('Pete');
		greet.salute()
		assert greet.getName() == 'Pete'
		assert greet.name == 'Pete'
	}
}
