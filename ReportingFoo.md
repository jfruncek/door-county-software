# Mavenized Open Reports #

  * Removed JFree (including Chart reports), Mondrian (including JPivot & WCF), BIRT, eSpreadsheet, Stark custom
  * Must mvn install missing (non-Maven) jars including JDBC drivers and Jasper - see doc/Jars\_added folder
  * Added a `ReportPostProcessor` interface (no impl)
  * Nearly works with jetty:run - issue with security
  * Should add a pure java db config

## Testing Reports ##

### What can we verify? ###

  * Runs for a given set of parameters
  * Tables exist with certain headings
  * Report is not "empty" (there are rows in the tables)
  * Data is not "missing"- defined as blanks, all zeroes
  * Verify totals against a check query or among reports
  * Correct "as of" date for parameter, fund description, report title
  * For some reports they don't like it if there's more than one page