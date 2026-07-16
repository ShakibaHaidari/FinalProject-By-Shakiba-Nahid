package server;

import com.sun.net.httpserver.HttpServer;
import service.GroupService;
import service.UserService;
import service.MessageService;
import service.SavedMessageService;
import service.ChatSettingService;
import service.BlockedUserService;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleHttpServer{

    private final UserService userService;
    private final GroupService groupService;
    private final MessageService messageService;
    private final SavedMessageService savedMessageService;
    private final ChatSettingService chatSettingService;
    private final BlockedUserService blockedUserService;

    private HttpServer server;
    private ExecutorService executor;

    public SimpleHttpServer(
            UserService userService,
            GroupService groupService,
            MessageService messageService,SavedMessageService savedMessageService, ChatSettingService chatSettingService,BlockedUserService blockedUserService){

        this.userService = userService;
        this.groupService = groupService;
        this.messageService = messageService;
        this.savedMessageService = savedMessageService;
        this.chatSettingService = chatSettingService;
        this.blockedUserService = blockedUserService;

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
        server.createContext(
                "/api/messages/edit",
                new EditMessageHandler(messageService)
        );

        server.createContext(
                "/api/messages/delete",
                new DeleteMessageHandler(messageService)
        );

        server.createContext(
                "/api/messages/history",
                new MessageHistoryHandler(messageService)
        );
        server.createContext(
                "/api/saved-messages",
                new SavedMessagesHandler(savedMessageService)
        );

        server.createContext(
                "/api/saved-messages/remove",
                new RemoveSavedMessageHandler(savedMessageService)
        );
        server.createContext(
                "/api/chats/pin",
                new PinChatHandler(chatSettingService)
        );

        server.createContext(
                "/api/chats/archive",
                new ArchiveChatHandler(chatSettingService)
        );

        server.createContext(
                "/api/chats/settings",
                new ChatSettingsHandler(chatSettingService)
        );
        server.createContext(
                "/api/users/block",
                new BlockUserHandler(blockedUserService)
        );

        server.createContext(
                "/api/users/unblock",
                new UnblockUserHandler(blockedUserService)
        );

        server.createContext(
                "/api/users/blocked",
                new BlockedUsersHandler(blockedUserService)
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

        System.out.println("Report API: POST /api/messages/report");

        System.out.println("Groups API: GET /api/groups");
        System.out.println("Edit Message API: POST /api/messages/edit");
        System.out.println("Delete Message API: POST /api/messages/delete");
        System.out.println("Message History API: GET /api/messages/history");
        System.out.println("Saved Messages API: GET/POST /api/saved-messages");
        System.out.println("Remove Saved Message API: POST /api/saved-messages/remove");
        System.out.println("Pin Chat API: POST /api/chats/pin");
        System.out.println("Archive Chat API: POST /api/chats/archive");
        System.out.println("Chat Settings API: GET /api/chats/settings");
        System.out.println("Block User API: POST /api/users/block");
        System.out.println("Unblock User API: POST /api/users/unblock");
        System.out.println("Blocked Users API: GET /api/users/blocked");
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
