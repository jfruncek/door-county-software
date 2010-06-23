package play

import static org.apache.commons.lang.WordUtils.*

class CoolGreet extends Greet {
	
	CoolGreet(who) {
		name = capitalize(who)
	}
	
	static def main(def args) {
		def g = new CoolGreet('sam')
		g.salute()
	}
}