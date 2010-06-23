def name = "Jim"
def multiLineQuote = """
Hello, ${name}
This is a multiline string with double quotes
"""
println multiLineQuote
println multiLineQuote.class.name
