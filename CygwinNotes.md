# Set Cygwin `/` #

This worked before but doesn't seem to now
```
mount "c:\" /
```

# Directory shortcuts #

Add symbolic links to the home directory to allow quick cd's to most used places and starting most used scripts. Examples:

  * ln -ds /cygdrive/c/evercore/trunk evercore
  * ln -ds /usr/local/tools/jboss/bin jboss

# Perl #

Verify which Perl is being used to confirm it's cygwin's. Make sure PERL5LIB includes cygwin's lib (may have others there like Oracle's). In particular, remove any non-cygwin, version-specific Perl libs and add
  * cygwin/lib/perl5/x.xx
  * cygwin/lib/perl5/vendor\_perl