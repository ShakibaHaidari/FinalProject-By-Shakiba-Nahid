import cli.AdminCLI;
import server.SimpleHttpServer;
import service.GroupService;
import service.UserService;
import service.MessageService;

public class Main {

    public static void main(String[] args) {

        UserService userService = new UserService();
        GroupService groupService = new GroupService();
        MessageService messageService = new MessageService(groupService);

        SimpleHttpServer server = new SimpleHttpServer(userService, groupService, messageService);

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