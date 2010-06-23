// Lists

def empty = []

println empty.class.name
println empty.size()

def list = ['Kara']

list.add 'Amy'
list << 'Beth'

println list.size()

list.each { println it }

println "first item: ${list[0]}"
println "first item (java syntax): ${list.get(0)}"
println "last item: ${list[-1]}"

list.remove 0
list -= 'Beth'

println list

// Maps

def emptyMap = [:]

println emptyMap.getClass().name

def todos = [a:'Write the map section', b:'Write the set section']

println todos.b

todos.put('c', 'write the kali section')

println todos




