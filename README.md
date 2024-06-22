# parameter-guard
A tool to add a runtime security layer for Java applications

Build Instructions: 
```
mvn clean compile assembly:single
```

and then move the *argument-gaurd-1.0-jar-with-dependencies.jar* to your project.
Make sure that you have configuration file *argument-guard.yaml* included in your resources directory.

Here is the sample content for the configuration file:
 *argument-gaurd-1.0-jar-with-dependencies.jar*

```
path: path/to/src
excludes: path/to/exclusions.txt
removeDetectedPattern: true
decompile: true
jar: path/to/app.jar
```

and create a *exclusions.txt* and include the patterns you want to drop and add the it's path to the configuration file.

*exclusions.txt*
```
_import__('os').system('rm -rf /')
/payload -O
cn=admin,cn=users,dc=domain,dc=com||password
nc -e /bin/sh
(|(&)
printf
("%s %s");
' OR '
\pom.xml
SELECT * FROM
rm -rf /
history | sh
pkill -STOP -u root
:(){ :|:& };:
^foo^bar
rm ~ *
rm -rf *
Chmod -R 777
cat /dev/null >
wget http://
wget https://
class.module.
dd if=/
crontab -r
command >
<em>
.java
DELETE FROM
del
rd
rmdir
diskpart
shutdown
chkdsk
gpupdate
taskkill
```
add this as your VM option to your application.
```
-javaagent:"path/to/argument-gaurd-1.0-jar-with-dependencies.jar" --add-opens java.base/java.lang=ALL-UNNAMED
```
