
class BindingValuesToClosure {
	
	def startTimer() {
		def initialDate = new Date()
		return { println "${initialDate} - ${new java.util.Date()} : Elapsed time {System.currentTimeMillis() - initialDate.time}" 
			   }
	}
			
	def timer = startTimer()

/* code at this point fails to compile!
	sleep 30000
    timer()
	// Simulate some more work
	sleep 30000
	timer()
	
	// Reset the timer
	println "Reset the Timer"
	timer = startTimer()
	timer()
	sleep 30000
	timer() */
}
