# Search SVN Repository #

```
svn list -vR svn://server/ | grep filename
```

or on Windows

```
svn list -vR svn://server/ | findstr filename
```

You can also add a path to a folder (commonly known as a repo)


# Creating a new project and putting it into SVN #

_This is written for Grails but applies to any new project_ [More info](http://www.grails.org/Checking+Projects+into+SVN)

This approach is for use when starting a new Grails project - i.e. it does not yet exist on your disk at all, nor in SVN. It will talk you through your first check in, including the relevant svn:ignore properties, and avoids any checkin of these ignored files that may or may not exist locally.

With SVN installed, create your project. We will use "MyProject" as the name for the purposes of demonstration:

```
grails create-app MyProject
cd <projname>
```
Now you must create an empty SVN directory under your SVN repository using whatever appropriate tools you have. This directory should be called the same as MyProject but it doesn't have to be. We will call this SVN repository directory path "emptysvndir" for this example.
Note: The WEB-INF instructions below are optional. If you perform those steps, you must run "grails upgrade" after checking out your repository.

Next you do:
```
svn checkout <svn-server-url>/emptysvndir/ .
svn add *
svn propset svn:ignore "WEB-INF" web-app/
svn propset svn:ignore "core" plugins/ (as of grails v1.0.3 there is no plugins/core directory)
svn rm --force web-app/WEB-INF
svn rm --force plugins/core
svn commit -m "First commit"
```
That should have your project committed, and changes to ./plugins/core and ./web-app/WEB-INF/ should be ignored by SVN.
You will not need to check out this project as we have used the "in place import" technique.

# Reverting #
