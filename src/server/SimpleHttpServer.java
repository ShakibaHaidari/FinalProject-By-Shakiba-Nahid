package server;

import com.sun.net.httpserver.HttpServer;
import service.GroupService;
import service.UserService;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleHttpServer {

    private final UserService userService;
    private final GroupService groupService;

    private HttpServer server;
    private ExecutorService executor;

    public SimpleHttpServer(
            UserService userService,
            GroupService groupService) {

        this.userService = userService;
        this.groupService = groupService;
    }

    public void start() throws Exception {

        server = HttpServer.create(
                new InetSocketAddress(8080),
                0
        );

        executor = Executors.newFixedThreadPool(10);

        server.setExecutor(executor);

        server.createContext("/", exchange -> {

            HttpUtils.addCorsHeaders(exchange);

            if (HttpUtils.handleOptions(exchange)) {
                return;
            }

            String response = "Chat server is running";

            byte[] bytes = response.getBytes(
                    StandardCharsets.UTF_8
            );

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "text/plain; charset=UTF-8"
            );

            exchange.sendResponseHeaders(
                    200,
                    bytes.length
            );

            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        server.createContext(
                "/api/signup",
                new SignupHandler(userService)
        );

        server.createContext(
                "/api/login",
                new LoginHandler(userService)
        );

        server.createContext(
                "/api/users",
                new UsersHandler(userService)
        );

        server.createContext(
                "/api/groups",
                new GroupsHandler(groupService)
        );

        server.start();

        System.out.println(
                "Server started: http://localhost:8080"
        );

        System.out.println(
                "Signup API: POST /api/signup"
        );

        System.out.println(
                "Login API: POST /api/login"
        );

        System.out.println(
                "Users API: GET /api/users"
        );

        System.out.println(
                "Groups API: GET /api/groups"
        );
    }

    public void stop() {

        if (server != null) {
            server.stop(0);
        }

        if (executor != null) {
            executor.shutdownNow();
        }
    }

}
