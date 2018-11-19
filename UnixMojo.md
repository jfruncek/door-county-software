Restart Jetty at night
```
crontab: 10 0 * * * ~/opt/jetty/bin/jetty.sh restart
```
Add sudoer on Centos
```
sudo usermod -aG wheel admin
```
Switch file formats on Linux
```
set ff=unix
```

Create a multiline file (also redirects stdout to file for other commands)

```
$ cat > fox.dog
The quick brown fox jumped over the lazy dog.
^D
```

Create a symbolic link. For example, to create a link to ant in /usr/local/bin:
```
$ ln -s apache-ant-1.8.1/bin/ant ant
```

Create an environment variable; use it in subsequent programs
```
myvar='A value that wont be expanded'
export myvar
```
Set terminal mode
```
TERM=vt100
```
Find ports on which something is listening
```
sudo lsof -nP | grep LISTEN
```
Find an open port / lookup associated PID
```
netstat -aon
ps -ef (cygwin: ps -eWf)
```

What is my IP on Solaris

```
ifconfig -a
```

List all users

```
cat /etc/passwd |grep "/home" |cut -d: -f1
```

Remove the .svn directories

```
find . -name '.svn' -exec rm -Rf '{}' \;
```

Copy source, removing the '.svn' directories.

```
dont know that I ever made this work
```

Grep select files recursively

```
 find . -name *.java | xargs -i grep -H stark {}
```

Remove files recursively

```
find . -type f | xargs -i rm {}
```

Remove certain numeric named directories

```
find -regextype posix-extended -regex '.+[[:digit:]]+$' | xargs -i rm -R {}
```

Find and copy gradle build results (jars)

```
 find . -regex '.*libs/.*jar' | xargs -i cp {} ../../tmp/jars/.
```

List disk space usage

```
du -h --max-depth=1 <dir>
```

Extract only certain files from a zip

```
 unzip -l 4144777710.zip | grep jpeg | sed 's/.* .* .* \(.*\)/\1/' | xargs -i unzip -j 4144777710_072314.zip {}
```
