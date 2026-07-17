package server;

import com.sun.net.httpserver.HttpServer;
import service.BlockedUserService;
import service.ChatSettingService;
import service.GroupService;
import service.MessageReactionService;
import service.MessageService;
import service.SavedMessageService;
import service.UserIdChangeService;
import service.UserService;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleHttpServer {

    private final UserService userService;
    private final GroupService groupService;
    private final MessageService messageService;
    private final SavedMessageService savedMessageService;
    private final ChatSettingService chatSettingService;
    private final BlockedUserService blockedUserService;
    private final MessageReactionService messageReactionService;
    private final UserIdChangeService userIdChangeService;

    private HttpServer server;
    private ExecutorService executor;


    public SimpleHttpServer(
            UserService userService,
            GroupService groupService,
            MessageService messageService,
            SavedMessageService savedMessageService,
            ChatSettingService chatSettingService,
            BlockedUserService blockedUserService,
            MessageReactionService messageReactionService
    ) {

        this.userService =
                userService;

        this.groupService =
                groupService;

        this.messageService =
                messageService;

        this.savedMessageService =
                savedMessageService;

        this.chatSettingService =
                chatSettingService;

        this.blockedUserService =
                blockedUserService;

        this.messageReactionService =
                messageReactionService;

        this.userIdChangeService =
                new UserIdChangeService(
                        userService,
                        groupService,
                        messageService,
                        savedMessageService,
                        chatSettingService
                );
    }


    public void start() throws Exception {

        server =
                HttpServer.create(
                        new InetSocketAddress(
                                8080
                        ),
                        0
                );

        executor =
                Executors.newFixedThreadPool(
                        10
                );

        server.setExecutor(
                executor
        );


        server.createContext(
                "/",
                exchange -> {

                    HttpUtils.addCorsHeaders(
                            exchange
                    );

                    if (HttpUtils.handleOptions(
                            exchange
                    )) {

                        return;
                    }

                    String response =
                            "Chat server is running";

                    byte[] bytes =
                            response.getBytes(
                                    StandardCharsets.UTF_8
                            );

                    exchange
                            .getResponseHeaders()
                            .set(
                                    "Content-Type",
                                    "text/plain; charset=UTF-8"
                            );

                    exchange.sendResponseHeaders(
                            200,
                            bytes.length
                    );

                    exchange
                            .getResponseBody()
                            .write(bytes);

                    exchange.close();
                }
        );


        server.createContext(
                "/api/signup",
                new SignupHandler(
                        userService
                )
        );


        server.createContext(
                "/api/login",
                new LoginHandler(
                        userService
                )
        );


        server.createContext(
                "/api/forgot-password",
                new ForgotPasswordHandler(
                        userService
                )
        );


        server.createContext(
                "/api/users",
                new UsersHandler(
                        userService
                )
        );


        server.createContext(
                "/api/users/activity",
                new UserActivityHandler(
                        userService
                )
        );


        server.createContext(
                "/api/contacts",
                new ContactsHandler(
                        userService
                )
        );


        server.createContext(
                "/api/groups",
                new GroupsHandler(
                        groupService
                )
        );


        server.createContext(
                "/api/messages",
                new MessagesHandler(
                        messageService,
                        userService
                )
        );


        server.createContext(
                "/api/media",
                new MediaHandler(
                        messageService,
                        userService
                )
        );


        server.createContext(
                "/api/messages/report",
                new ReportMessageHandler(
                        messageService
                )
        );


        server.createContext(
                "/api/messages/edit",
                new EditMessageHandler(
                        messageService
                )
        );


        server.createContext(
                "/api/messages/delete",
                new DeleteMessageHandler(
                        messageService
                )
        );


        server.createContext(
                "/api/messages/history",
                new MessageHistoryHandler(
                        messageService
                )
        );


        server.createContext(
                "/api/saved-messages",
                new SavedMessagesHandler(
                        savedMessageService
                )
        );


        server.createContext(
                "/api/saved-messages/remove",
                new RemoveSavedMessageHandler(
                        savedMessageService
                )
        );


        server.createContext(
                "/api/chats/pin",
                new PinChatHandler(
                        chatSettingService
                )
        );


        server.createContext(
                "/api/chats/archive",
                new ArchiveChatHandler(
                        chatSettingService
                )
        );


        server.createContext(
                "/api/chats/settings",
                new ChatSettingsHandler(
                        chatSettingService
                )
        );


        server.createContext(
                "/api/users/block",
                new BlockUserHandler(
                        blockedUserService
                )
        );


        server.createContext(
                "/api/users/unblock",
                new UnblockUserHandler(
                        blockedUserService
                )
        );


        server.createContext(
                "/api/users/blocked",
                new BlockedUsersHandler(
                        blockedUserService
                )
        );


        server.createContext(
                "/api/messages/react",
                new ReactMessageHandler(
                        messageReactionService
                )
        );


        server.createContext(
                "/api/messages/reaction/remove",
                new RemoveReactionHandler(
                        messageReactionService
                )
        );


        server.createContext(
                "/api/messages/reactions",
                new MessageReactionsHandler(
                        messageReactionService
                )
        );


        server.createContext(
                "/api/chats/info",
                new ChatInfoHandler(
                        userService,
                        groupService
                )
        );


        server.createContext(
                "/api/account/settings",
                new AccountSettingsHandler(
                        userService
                )
        );


        server.createContext(
                "/api/account/profile-picture",
                new ProfilePictureHandler(
                        userService
                )
        );


        server.createContext(
                "/api/account/change-id",
                new ChangeUserIdHandler(
                        userIdChangeService
                )
        );


        server.createContext(
                "/api/groups/profile-picture",
                new GroupProfilePictureHandler(
                        groupService
                )
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
                "User Activity API: GET/POST /api/users/activity"
        );

        System.out.println(
                "Contacts API: GET /api/contacts"
        );

        System.out.println(
                "Groups API: GET/POST /api/groups"
        );

        System.out.println(
                "Messages API: GET/POST /api/messages"
        );

        System.out.println(
                "Media API: GET/POST /api/media"
        );

        System.out.println(
                "Report API: POST /api/messages/report"
        );

        System.out.println(
                "Edit Message API: POST /api/messages/edit"
        );

        System.out.println(
                "Delete Message API: POST /api/messages/delete"
        );

        System.out.println(
                "Message History API: GET /api/messages/history"
        );

        System.out.println(
                "Saved Messages API: GET/POST /api/saved-messages"
        );

        System.out.println(
                "Remove Saved Message API: POST /api/saved-messages/remove"
        );

        System.out.println(
                "Pin Chat API: POST /api/chats/pin"
        );

        System.out.println(
                "Archive Chat API: POST /api/chats/archive"
        );

        System.out.println(
                "Chat Settings API: GET /api/chats/settings"
        );

        System.out.println(
                "Block User API: POST /api/users/block"
        );

        System.out.println(
                "Unblock User API: POST /api/users/unblock"
        );

        System.out.println(
                "Blocked Users API: GET /api/users/blocked"
        );

        System.out.println(
                "React Message API: POST /api/messages/react"
        );

        System.out.println(
                "Remove Reaction API: POST /api/messages/reaction/remove"
        );

        System.out.println(
                "Message Reactions API: GET /api/messages/reactions"
        );

        System.out.println(
                "Chat Info API: GET/POST /api/chats/info"
        );

        System.out.println(
                "Account Settings API: GET/POST /api/account/settings"
        );

        System.out.println(
                "Profile Picture API: GET/POST /api/account/profile-picture"
        );

        System.out.println(
                "Change User ID API: POST /api/account/change-id"
        );

        System.out.println(
                "Group Picture API: GET/POST /api/groups/profile-picture"
        );
    }


    public void stop() {

        if (server != null) {

            server.stop(
                    0
            );
        }


        if (executor != null) {

            executor.shutdownNow();
        }
    }
}