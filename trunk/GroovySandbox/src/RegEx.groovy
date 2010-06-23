
// Match operator (exact match)
assert "abc" ==~ 'abc'
assert !('abcabc' ==~ /abc/)
assert 'abc' ==~ /^a../

// Find operator
def winpath=/C:\windows\system32\somedir/
def matcher = winpath =~ /(\w{1}):\\(\w+)\\(\w+)\\(\w+)/
println matcher
println matcher[0] // ["C:\windows\system32\somedir", "C", "windows", "system32", "somedir"]
println matcher[0][1] // C

def newPath = matcher.replaceFirst('/etc/bin/')
println newPath // /etc/bin

// Pattern (compiled)
def saying = """Now is the time for all good men (and women) to come to the aid
 of their country"""
def pattern = ~/(\w+en)/
def matcher2 = pattern.matcher(saying)
def count = matcher2.getCount()
println "Matches = ${count}"
for(i in 0..<count) {
	println matcher2[i]
}

// Using regex to parse a phone number into a domain class
class Phone {
	String areaCode
	String exchange
	String local
}

def phoneStr = '(800)555-1212'
def phonePattern = ~/^[01]?\s*[\(\.-]?(\d{3})[\)\.-]?\s*(\d{3})[\.-](\d{4})$/
def matcher3 = phonePattern.matcher(phoneStr)
def phone = new Phone(
		areaCode: matcher3[0][1],
		exchange: matcher3[0][2],
		local: matcher3[0][3])
println "Original Phone Number: ${phoneStr}"
println """Parsed Phone Number\
	\n\tArea Code = ${phone.areaCode}\
	\n\tExchange = ${phone.exchange}\
	\n\tLocal = ${phone.local}"""