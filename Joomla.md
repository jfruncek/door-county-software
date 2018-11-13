# How To's #

http://kb.siteground.com/article/How_to_move_Joomla_to_another_directory.html


# Installation procedure on Ubuntu 8.04 #

Following Quick Start at http://www.joomla.org/download.html .

  * Install http://www.apachefriends.org/en/xampp-linux.html

```
tar xvfz xampp-linux-1.7.3a.tar.gz -C /opt
```

  * Must use `mysql` provided with xampp, so first

```
mysql shutdown
```

  * Start xampp

```
/opt/lampp/lampp start
```

  * Check XAMPP using localhost. Need to secure later using

```
/opt/lampp/lampp security
```

  * Made a web directory for Joomla, downloaded and extracted there.

```
root@frubuntu:/opt/lampp/htdocs# mkdir joomla15
root@frubuntu:/opt/lampp/htdocs/joomla15/ # unzip -l /home/john/Destop/Joomla_1.5.15-Stable-Full_Package.zip
```

  * Finish Joomla install http://localhost/joomla15

```
created joomla15/configuration.php from default in textbox
moved installation folder to /tmp (or delete it)
```

# Usage #

  * Use the correct `mysql`
```
root@frubuntu:/opt/lampp/bin/mysql (need to secure later)
```

  * Hitting Admin or default page gives lots of "Strict.." errors. The following line in etc/php.ini does the trick (restart of lampp needed)

```
error_reporting = E_ALL & ~E_NOTICE
```

  * Opening some directories to write for all (should really determine what Joomla is running as and add this user to a group with write permissions)

```
/opt/lampp/htdocs/joomla15/components
/opt/lampp/htdocs/joomla15/language
/opt/lampp/htdocs/joomla15/media
/opt/lampp/htdocs/joomla15/plugins
/opt/lampp/htdocs/joomla15/plugins/system
/opt/lampp/htdocs/joomla15/tmp
/opt/lampp/htdocs/joomla15/administrator/component
/opt/lampp/htdocs/joomla15/administrator/modules
```