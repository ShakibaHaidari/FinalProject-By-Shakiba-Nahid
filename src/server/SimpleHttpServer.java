package server;

import com.sun.net.httpserver.HttpServer;
import service.GroupService;
import service.UserService;
import service.MessageService;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleHttpServer {

    private final UserService userService;
    private final GroupService groupService;
    private final MessageService messageService;

    private HttpServer server;
    private ExecutorService executor;

    public SimpleHttpServer(
            UserService userService,
            GroupService groupService,
            MessageService messageService) {

        this.userService = userService;
        this.groupService = groupService;
        this.messageService = messageService;
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
                "/api/forgot-password",
                new ForgotPasswordHandler(userService)
        );
        server.createContext(
                "/api/users",
                new UsersHandler(userService)
        );

        server.createContext(
                "/api/groups",
                new GroupsHandler(groupService)
        );
        server.createContext(
                "/api/messages",
                new MessagesHandler(
                        messageService,
                        userService
                )
        );

        server.createContext(
                "/api/messages/report",
                new ReportMessageHandler(messageService)
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
                "Forgot Password API: POST /api/forgot-password"
        );
        System.out.println(
                "Users API: GET /api/users"
        );
        System.out.println(
                "Messages API: GET/POST /api/messages"
        );

        System.out.println(
                "Report API: POST /api/messages/report"
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
