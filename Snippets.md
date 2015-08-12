# JSP #

## Output a map (for debugging) ##

```
<s:iterator id="entry" value="scheduleParameters">
  <tr>
    <td>
      <s:property value="key"/>
    </td>
    <td>
      <s:property value="value"/>
    </td>
  </tr>
</s:iterator>
```

# Javascript #

## Add to Google calendar ##

```
javascript:%20AddToCalendar('google',%20'https://www.eventbrite.com',%20'580571505',%20'');
```

# Java #

## Send email from command line ##
```
$ java -cp '.\bin;C:\evercore\trunk\jboss\server\ti2m\lib\mail.jar' utilities.MsgSend -M 172.16.10.4 -s 'Software Developer Internship' -o careers@teramedica.com -b jfruncek@teramedica.com -i thankyou.txt 'name <email@gmail.com>'
```

# Groovy #

## Maps to use as stubs ##

```
def query = [list : new Object()]
def hibernateSession = [createQuery: query] as Session
```

## Print Root Loader URLs ##

```
this.class.classLoader.rootLoader.URLs.each{ println it }
```

# JVM #

## Verbose class loading ##

```
set JAVA_OPTS=-verbose:class
```