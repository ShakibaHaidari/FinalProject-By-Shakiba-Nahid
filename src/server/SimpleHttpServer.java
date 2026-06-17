package server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class SimpleHttpServer {

    public void start() throws Exception {

        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(8080),
                        0
                );

        server.createContext("/", exchange -> {

            String response =
                    "Chat Server Running";

            exchange.sendResponseHeaders(
                    200,
                    response.length()
            );

            exchange.getResponseBody()
                    .write(response.getBytes());

            exchange.close();
        });

        server.createContext(
                "/login",
                new LoginHandler()
        );
        server.start();

        System.out.println(
                "Server started on port 8080"
        );
    }
}