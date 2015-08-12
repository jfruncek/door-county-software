# Conventions #
  * Standard directory layout for projects
  * A single Maven project produces a single output
  * Standard naming conventions (such as `commons-logging-1.2.jar`

Maven's Super POM carries with it all the default conventions that Maven encourages, and is the analog of the Java language's java.lang.Object class.

# Dependencies #

  * The dependencies contained within the `dependencyManagement` element are used only to state the preference for a version and by themselves do not affect a project's dependency graph, whereas the top-level `dependencies` element does. _This means that a dependency **must** be inherited or explicitly declared in the `dependencies` element. Also version is required in the `dependencyManagement` element but not in the `dependencies` element_
  * order of elements doesn't matter

# Plugins #
  * everything accomplished in Maven is the result of a plugin executing
  * execution is coordinated by Maven's build life cycle in a declarative fashion
  * To change compiler settings, use
```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>2.0.2</version>
  <configuration>
    <source>6</source>
    <target>6</target>
  </configuration>
</plugin>
```
  * To ignore test results and continue the build
```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <testFailureIgnore>true</testFailureIgnore>
  </configuration>
</plugin>
```

# Links #

[Better Builds with Maven](http://www.maestrodev.com/better-build-maven)

[Maven- The Definitive Guide](http://door-county-software.googlecode.com/files/maven-definitive-guide.pdf)

[Convert Ant to Maven](http://www.sonatype.com/people/2009/04/how-to-convert-from-ant-to-maven-in-5-minutes/)

[Obtaining Spring 3 with Maven](http://blog.springsource.com/2009/12/02/obtaining-spring-3-artifacts-with-maven/)

[Maven Central](http://repo1.maven.org/maven2/)

[The Maven Project](http://maven.apache.org/)

[Maven Standard Directory layout](http://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html)

[Building Web Applications with Maven2](http://today.java.net/pub/a/today/2007/03/01/building-web-applications-with-maven-2.html)

# Interesting Maven commands #
| **Command** | **Description** | **Link** |
|:------------|:----------------|:---------|
|`mvn -h`     |Maven command line options|          |
|`mvn help:describe -Dplugin=org.apache.maven.plugins:maven-jarsigner-plugin -Ddetail` | describes goals and parameters for a plugin |          |
|`mvn help:effective-pom`|Combines the project POM with the contents of all parent POMs, user settings, and any active profiles. This goal can come in handy if you are trying to debug a build and want to see how all of the current project's ancestor POMs are contributing to the effective POM. |[Inheritance](http://maven.apache.org/pom.html#Inheritance) |
|`mvn help:evaluate`|interactively evaluate maven expressions, for example you can use this to display the value of a property |          |
|`mvn dependency:analyze`|finds undeclared dependencies and unused declared dependencies|[Dependency Plugin](http://maven.apache.org/plugins/maven-dependency-plugin/usage.html) |
|`mvn dependency:tree`| displays dependencies in a tree |          |
|`mvn dependency:tree -Dverbose -Dincludes=commons-collections`|limits scope of above|          |
| ` mvn deploy:deploy-file -DgroupId=com.actuate -DartifactId=espreadsheet -Dversion=9.1 -Dpackaging=pom -Dfile=../com.actuate/pom.xml -DrepositoryId=mke-sisp-01.starkinvestments.com-releases -Durl=http://mke-sisp-01:5080/artifactory/third-party ` | Deploy a resource to a local repo |          |

# Gotchas #
[Mysterious missing JMS jar](http://www.mail-archive.com/users@maven.apache.org/msg106643.html) - Run any goal will show you how to download and install manually the missing jars. (You can also try the [Sun java.net repo](http://maven.apache.org/guides/mini/guide-coping-with-sun-jars.html)) The commands you will need are
```
$ mvn install:install-file -DgroupId=javax.jms -DartifactId=jms -Dversion=1.1 -Dpackaging=jar -Dfile=jms.jar
$ mvn install:install-file -DgroupId=com.sun.jdmk -DartifactId=jmxtools -Dversion=1.2.1 -Dpackaging=jar -Dfile=jmxtools.jar
$ mvn install:install-file -DgroupId=com.sun.jmx -DartifactId=jmxri -Dversion=1.2.1 -Dpackaging=jar -Dfile=jmxri.jar
```
**Running Jetty on Windows** If you are running the Maven Jetty Plugin on a Windows platform you may need to move your local Maven repository to a directory that does not contain spaces. Some have reported issues on Jetty startup caused by a repository that was being stored under `C:\Documents and Settings\<user>`. The solution to this problem is to move your local Maven repository to a directory that does not contain spaces and redefine the location of your local repository