import cli.AdminCLI;
import server.SimpleHttpServer;
import service.BlockService;
import service.BlockedUserService;
import service.ChatSettingService;
import service.GroupService;
import service.MessageReactionService;
import service.MessageService;
import service.SavedMessageService;
import service.UserService;
import storage.DataPaths;
import websocket.WebSocketServer;

public class Main {

    public static void main(String[] args) {

        DataPaths.initialize();

        UserService userService =
                new UserService();

        GroupService groupService =
                new GroupService();

        MessageService messageService =
                new MessageService(
                        groupService
                );

        BlockService blockService =
                new BlockService(
                        userService
                );

        messageService.setBlockService(
                blockService
        );

        SavedMessageService savedMessageService =
                new SavedMessageService(
                        messageService
                );

        ChatSettingService chatSettingService =
                new ChatSettingService();

        BlockedUserService blockedUserService =
                new BlockedUserService();

        MessageReactionService messageReactionService =
                new MessageReactionService(
                        messageService
                );

        SimpleHttpServer server =
                new SimpleHttpServer(
                        userService,
                        groupService,
                        messageService,
                        savedMessageService,
                        chatSettingService,
                        blockedUserService,
                        messageReactionService
                );

        WebSocketServer webSocketServer =
                new WebSocketServer(
                        9090,
                        messageService
                );

        Thread webSocketThread =
                new Thread(
                        () -> webSocketServer.start()
                );

        webSocketThread.start();


        AdminCLI adminCLI =
                new AdminCLI(
                        userService,
                        groupService
                );


        Thread serverThread =
                new Thread(
                        () -> {

                            try {

                                server.start();

                            } catch (Exception exception) {

                                System.out.println(
                                        "Could not start server: "
                                                + exception.getMessage()
                                );
                            }
                        }
                );

        serverThread.start();

        adminCLI.start();

        server.stop();
    }
}