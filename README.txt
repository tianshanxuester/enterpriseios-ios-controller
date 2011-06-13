=== Server and Environment configuration ===
etc/bean.xml - to change listen port and the URI of the DMS server to which device activation HTTP post request should be made.
etc/cumin.properties - to configure data store settings
resources/log4j.properties - log4j settings
resources/log4j.xml - log4j settings

=== SSL Certificates ===

Instructions for loading your SSL certificate into a keystore are available on the wiki:
  http://docs.codehaus.org/display/JETTY/How+to+configure+SSL

If you have an existing certificate, and simply need to load it into the keystore, you can start from Step 3:
  http://docs.codehaus.org/display/JETTY/How+to+configure+SSL#HowtoconfigureSSL-step3

=== Installing from Source and Running ===

To install:
cd cumin
mvn install

To run:
cd jetty-push-distribution/target/distribution
java -jar start.jar

the start.ini file should contain the following entries.

#===========================================================
# Jetty start.jar arguments
#-----------------------------------------------------------
--exec
-Xmx768m
-Dorg.eclipse.jetty.util.log.INFO=true
OPTIONS=Server,push,resources,client
etc/jetty-ssl.xml
etc/jetty-logging.xml
start.class=com.enterpriseios.push.spring.Main
etc/spring/bean.xml

=== Project Flow and Structure ===

=> Project module overview
The inventit-push project is made up of two maven modules, tied together by a cumin project pom.xml file and some dependencies found in its parent pom.xml:
* jetty-push2 - contains the ActiveSync server
* jetty-push-distribution - pulls together the components into a distribution bundle

=> Configuring the ActiveSync server
* jetty-push2/src/main/resources/etc/spring/bean.xml - server configuration (ports to listen on, domain to map to, keystore and truststore locations and passwords, filter files location, etc)
* jetty-push2/src/main/distribution/start.ini - start up arguments