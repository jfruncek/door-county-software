Add sudoer on Centos
```
sudo usermod -aG wheel admin
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

Create tarball of certain files including paths

```
 find .jenkins/jobs -name config.xml -exec tar -rvf jobs.tar '{}' \;
```

Delete duplicate lines in a file without sorting it

```
awk '!seen[$0]++' file.txt
```

Extract only certain files from a zip

```
 unzip -l 4144777710.zip | grep jpeg | sed 's/.* .* .* \(.*\)/\1/' | xargs -i unzip -j 4144777710_072314.zip {}
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

Grep select files recursively

```
 find . -name *.java | xargs -i grep -H stark {}
```

List all users

```
cat /etc/passwd |grep "/home" |cut -d: -f1
```

Remove the .svn directories

```
find . -name '.svn' -exec rm -Rf '{}' \;
```

Remove files older than a week

```
find /tmp -type f -mtime +7 -exec rm -f {} \;
```
Restart Jetty at night
```
crontab: 10 0 * * * ~/opt/jetty/bin/jetty.sh restart
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

Set terminal mode
```
TERM=vt100
```

Show CPU info

```
lscpu
```

Show disk space usage

```
du -h --max-depth=1 <dir>
```

Show memory chips intalled

```
sudo dmidecode --type memory
sudo lshw -c memory
```

Switch file formats on Linux

```
set ff=unix
```

What is my IP? (internal; for external use https://www.whatismyip.com/)

```
hostname -I (Ubuntu, minimal)
ip addr show (Ubuntu, all)
ifconfig -a (Solaris)
```
