package play;

class Greet {
	
	def name
	
	Greet() {}
	
	Greet(who) {
		name = who[0].toUpperCase() + who[1..-1]
	}
	
	def salute() {
		println "Hello $name!"
	}
	
	static def main(def args) {
		def g = new Greet(args[0])
		g.salute()
	}
	
}


