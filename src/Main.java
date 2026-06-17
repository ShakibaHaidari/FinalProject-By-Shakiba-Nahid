//import cli.AdminCLI;
//import service.GroupService;
//import service.UserService;
//
//public class Main {
//
//    public static void main(String[] args) {
//
//        UserService userService =
//                new UserService();
//
//        GroupService groupService =
//                new GroupService();
//
//        AdminCLI cli =
//                new AdminCLI(
//                        userService,
//                        groupService
//                );
//
//        cli.start();
//    }
//}
import server.SimpleHttpServer;

public class Main {

    public static void main(String[] args)
            throws Exception {

        SimpleHttpServer server =
                new SimpleHttpServer();

        server.start();
    }
}