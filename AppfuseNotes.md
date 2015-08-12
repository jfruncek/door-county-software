# Prerequisites #

Testing to point done on cygwin at `/usr/local/tools` in `c:/cygwin`

**Java (jdk1.6.0\_03 - jdk1.6.0\_14 tested)**

Set environment variables: PATH = %PATH%;%ANT\_HOME%/bin;%JAVA\_HOME%/bin

**Database (mysql-5.0.45-win32 and hsqldb tested)**

```
> nohup ./mysqld-nt.exe &   #starts mysql, next change root pwd
> ./mysql.exe --user=root --pass='' mysql -e "update user set password=password('') where user='root'; flush privileges;"
> ./mysql.exe --user=root --pass=''   #creates a client window for sql
```

hsqldb works via profile choice in pom when running `mvn jetty:run-war`

**Maven (maven-2.0.7 and Eclipse embedded tested)**

**SMTP server (optional) (james-2.3.1 tested)**

```
> nohup ./run.sh &
```

# New Project #

  * From ~/dev, run the maven archetype for your chosen stack (example uses Struts 2 & Hibernate)

```
> mvn archetype:create -DarchetypeGroupId=org.appfuse.archetypes -DarchetypeArtifactId=appfuse-basic-struts -DremoteRepositories=http://static.appfuse.org/releases -DarchetypeVersion=2.0 -DgroupId=com.door.app -DartifactId=myproject 
```

  * Run and view the application (this example uses HSQL DB)

```
> cd <new project's directory>
> mvn jetty:run-war -Phsqldb
Browse to http://localhost:8080
```

  * (optional) Import project into subversion (uses Google code's generated password stored by svn)

```
> svn import -m "First project" myproject https://appfuse2.googlecode.com/svn/trunk/myproject --username fishribs
> svn list http://appfuse2.googlecode.com/svn/trunk/  #lists svn repo
> mv myproject myproject.1  #moves current copy out of the way
> svn checkout http://appfuse2.googlecode.com/svn/trunk/myproject
```


# Create and install an entity #

[Appfuse uses Maven plugins](http://appfuse.org/display/APF/Maven+Plugins) for code generation (and a lot else). You can also follow the [tutorials](http://appfuse.org/display/APF/Tutorials). But of course you still need to start with either a POJO model or a database table (one or the other of the first two below).

  * Create a POJO in the <group.Id>.model package. At minimum you will need something like
```
package com.door.app.model;

import javax.persistence.*;
@Entity @Table(name="person")
public class Person {
    Long id;
    String firstName;
    String lastName;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
```
  * Generate an issue entity by running mvn appfuse:gen-model after creating the table.
```
create table issue 
 (id bigint not null auto_increment, 
  primary key (id),
  summary varchar(80),
  reporter varchar(40),
  assignee varchar(40),
  status int,
  priority int,
  severity int,
  resolution int,
  created datetime
  ) 
```
  * Generate CRUD
```
> mvn appfuse:gen -Dentity=Issue -DdisableInstallation=true 
```
  * Generates `IssueAction.java` in
```
     /src/main/java/com/door/issuetracker/webapp/action/
```
  * Adds the entity to `hibernate.cfg.xml` in
```
     /src/main/resources
```
  * Generates `Issue-validation.xml` (empty since no constraints)
```
    /src/main/resources/com/door/issuetracker/model
```
  * Not sure about `IssueAction-validation.xml`
```
    /src/main/resources/com/door/issuetracker/webapp/action
```
  * Generates `issueForm.jsp` & `issueList.jsp` in
```
    /src/main/webapp/WEB-INF/pages
```
  * Generates test class `IssueActionTest.java` in
```
    /src/test/java/com/door/issuetracker/webapp/action
```

  * Install the entity with mvn appfuse:install -Dentity=Issue

  * Adds page messages (including table headings) to `ApplicationResources.properties`
  * Adds action - result mappings to `struts.xml`
```
     /src/main/resources
```
  * Adds main menu item to menu.jsp
```
    /src/main/webapp/common
```
  * Adds DAO to `applicationContext.xml`
  * Adds menu target page mapping to `menu-config.xml`
```
    /src/main/webapp/WEB-INF/
```
  * Adds test data to `sample-data.xml`
  * Adds canoo tests to `web-tests.xml`
```
    /src/test/resources
```

```
 tomcat/conf/tomcat-users.xml
<user username="jfruncek" password="" roles="admin,manager"/>

```

# Tutorials #
[Quick Start](http://appfuse.org/display/APF/AppFuse+QuickStart)
[Appfuse Demos](http://appfuse-demos.googlecode.com/svn/trunk)

## Error starting Sun's native2ascii: ##

This is caused by the native2ascii plugin for Maven- remove it from the pom. [Search this for more info](http://appfuse.markmail.org)

## Annotation compile issue ##

[Solution](http://issues.appfuse.org/browse/APF-1072)