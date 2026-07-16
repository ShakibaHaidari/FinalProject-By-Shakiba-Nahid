import cli.AdminCLI;
import server.SimpleHttpServer;
import service.GroupService;
import service.UserService;
import service.MessageService;
import storage.DataPaths;
import websocket.WebSocketServer;
import service.SavedMessageService;
import service.ChatSettingService;
public class Main {

    public static void main(String[] args) {
        DataPaths.initialize();
        UserService userService = new UserService();
        GroupService groupService = new GroupService();
        MessageService messageService = new MessageService(groupService);
        SavedMessageService savedMessageService = new SavedMessageService(messageService);
        ChatSettingService chatSettingService = new ChatSettingService();

        SimpleHttpServer server = new SimpleHttpServer(userService, groupService, messageService , savedMessageService,chatSettingService
        );
//        creat websocket
        WebSocketServer webSocketServer =new WebSocketServer(9090,  messageService);
        Thread webSocketThread = new Thread(() -> {
            webSocketServer.start();
        });

        webSocketThread.start();

        AdminCLI adminCLI = new AdminCLI(userService, groupService);

        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception exception) {
                System.out.println(
                        "Could not start server: "
                                + exception.getMessage()
                );
            }
        });
        serverThread.start();
        adminCLI.start();
        server.stop();
    }
}