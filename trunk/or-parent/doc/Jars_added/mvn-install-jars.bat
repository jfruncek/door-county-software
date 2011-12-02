echo installing jars- Maven issues

cmd /C mvn install:install-file -DgroupId=javax.jms -DartifactId=jms -Dversion=1.1 -Dpackaging=jar -Dfile=jms.jar

cmd /C mvn install:install-file -DgroupId=com.sun.jdmk -DartifactId=jmxtools -Dversion=1.2.1 -Dpackaging=jar -Dfile=jmxtools.jar

cmd /C mvn install:install-file -DgroupId=com.sun.jmx -DartifactId=jmxri -Dversion=1.2.1 -Dpackaging=jar -Dfile=jmxri.jar

cmd /C mvn install:install-file -DgroupId=xmlbeans -DartifactId=xmlbeans -Dversion=2.4.0 -Dpackaging=jar -Dfile=xmlbeans-2.4.0.jar

echo installing jars- Open Reports issues

cmd /C mvn install:install-file -DgroupId=net.socialchange.doctype -DartifactId=doctype-changer -Dversion=1.1 -Dpackaging=jar -Dfile=DoctypeChanger.jar

cmd /C mvn install:install-file -DgroupId=net.sf.docbook -DartifactId=docbook -Dversion=1.74.0 -Dpackaging=jar -Dfile=docbook.jar

cmd /C mvn install:install-file -DgroupId=com.googlcode.strut2gwtplugin -DartifactId=struts2gwtplugin -Dversion=1.02 -Dpackaging=jar -Dfile=struts2gwtplugin-1.02.jar

cmd /C mvn install:install-file -DgroupId=javassist -DartifactId=javassist -Dversion=3.12.1.GA -Dpackaging=jar -Dfile=javassist-3.12.1.GA.jar
