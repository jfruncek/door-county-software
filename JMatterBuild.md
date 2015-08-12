
```
JMatter Build Notes

(This trace in cygwin)

$ svn co http://svn.jmatter.org/jmatter-complet/tags/Release-20080902/ jmatter
...
Checked out revision 1522.

$ ant -version
Apache Ant version 1.7.0 compiled on December 13 2006

$ cd jmatter ; ant

...
raw-compile:
    [javac] Compiling 303 source files to c:\dev\jmatter\jmatter\build\classes
    [javac] Note: Some input files use unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.
     [copy] Copying 148 files to c:\dev\jmatter\jmatter\build\classes
[native2ascii] Converting 6 files from c:\dev\jmatter\jmatter\resources to c:\dev\jmatter\jmatter\build\classes
...
makedoc-pdf:

BUILD FAILED
c:\dev\jmatter\build.xml:74: Execute failed: java.io.IOException: CreateProcess: lyx --export pdf2 guide.lyx error=2

Total time: 59 seconds

( Install lyx editor in cygwin )

make-distribution:
     [copy] Copying 628 files to c:\dev\jmatter\build\jmatter
     [copy] Copied 66 empty directories to 6 empty directories under c:\dev\jmatter\build\jmatter

BUILD FAILED
c:\dev\jmatter\build.xml:42: Execute failed: java.io.IOException: CreateProcess: markdown jMatter-License error=2

( Install markdown text to XHTML converter )

```