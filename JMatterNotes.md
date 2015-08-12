# Initial Setup #

  1. Download latest from Latest JMatter Framework and unpack as follows.
```
$ pwd
/home/john
$ tar -xzvf Desktop/jmatter-20081002.tgz 
```

  1. Create a NetBeans 6.1 project and template for the JMatter ACO object.
  1. Import project to Googlecode

```
$ svn import myTunes https://door-county-software.googlecode.com/svn/trunk -m 'initial import'
```


# Setup #

  1. Launch NetBeans (using 6.5 as of Dec 2008) and from menu Versioning/Subversion, checkout from http://door-county-software.googlecode.com/svn/trunk to /home/john/jmatter-20081002 anonymously. IDE generates trace below and asks if you wish to open the project.
```
co -r HEAD http://door-county-software.googlecode.com/svn/trunk/myTunes /home/john/jmatter-20081002/myTunes --config-dir /home/john/.netbeans/6.5/config/svn/config --non-interactive
...
A    /home/john/jmatter-20081002/myTunes/build.xml
A    /home/john/jmatter-20081002/myTunes/nbbuild.xml
Checked out revision 13.
```

  1. Configure NetBeans for SVN.
  * While logged in as fishribs Door County Software click Profile/Settings and grab the password.
  * In NetBeans, Tools / Options / Miscellaneous / Versioning / Manage Connections - this doesn't allow


Dec 19 2008

1. Try build- reports non-existing c:/dev/...

**Change framework.dir property to "." !! <-- don't do this** Build now reports missing lib/tools directory.
**Think: Must not have imported some binaries- try copying lib from another project.** Not it. Try generating a new project and comparing.
**Problem was none of the above- the framework.dir is home/john/jmatter-20081002/jmatter (copied from the new project)**

ant schema-export
ant

2.  Add a user.properties so that framework.dir can be specified (file will not be committed).

**Create user.properties with the content below.**

jmatter.home=/home/john/jmatter-20081002

**Added the line below to the build.xml**

> 

&lt;property file="user.properties" /&gt;

 <!-- added by jfruncek -->

3. Try to Configure NetBeans for SVN.

**While logged in as fishribs Door County Software click Profile/Settings and grab the password.** In NetBeans, Tools / Options / Miscellaneous / Versioning / Manage Connections - this doesn't allow editing the URL
**No SVN settings in project properties**

4. Decide to give up on SVN for now and start section 5.10.

Dec 27



