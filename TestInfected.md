# Uncle bob's Prime Factors #

Code in Door Sandbox. Lessons learned:
  * Dropped back to `sysout` when tests failed, which wasn't terrible, but could more tests have been written on smaller methods instead?
  * Need to add more robust test data set? Reading tests from a simple text file is a good idea!
  * Also need to refactor
  * Instance of `PrimeFactors` was easier to test than static class

# Venkat's thread safe multi-valued map #

  * Wow, using the `Lock` interface makes it worlds easier to test