package cli;

import model.Group;
import model.User;
import service.GroupService;
import service.UserService;

import java.util.Scanner;

public class AdminCLI {

    private final Scanner scanner =
            new Scanner(System.in);

    private final UserService userService;
    private final GroupService groupService;

    public AdminCLI(UserService userService,
                    GroupService groupService) {

        this.userService = userService;
        this.groupService = groupService;
    }

    public void start() {

        while (true) {

            System.out.println("""
                    
                    ===== ADMIN MENU =====
                    1. Add User
                    2. List Users
                    3. Remove User
                    4. Add Group
                    5. List Groups
                    6. Remove Group
                    0. Exit
                    """);

            int choice =
                    Integer.parseInt(scanner.nextLine());

            switch (choice) {

                case 1 -> addUser();
                case 2 -> listUsers();
                case 3 -> removeUser();
                case 4 -> addGroup();
                case 5 -> listGroups();
                case 6 -> removeGroup();
                case 0 -> {
                    return;
                }
            }
        }
    }

    private void addUser() {

        System.out.print("ID: ");
        String id = scanner.nextLine();

        System.out.print("Username: ");
        String username =
                scanner.nextLine();

        System.out.print("Password: ");
        String password =
                scanner.nextLine();

        userService.addUser(
                new User(id,
                        username,
                        password));

        System.out.println("User Added.");
    }

    private void listUsers() {

        for (User user :
                userService.getUsers()) {

            System.out.println(user);
        }
    }

    private void removeUser() {

        System.out.print("User ID: ");

        String id =
                scanner.nextLine();

        userService.removeUser(id);
    }

    private void addGroup() {

        System.out.print("Group ID: ");

        String id =
                scanner.nextLine();

        System.out.print("Group Name: ");

        String name =
                scanner.nextLine();

        groupService.addGroup(
                new Group(id, name));
    }

    private void listGroups() {

        for (Group group :
                groupService.getGroups()) {

            System.out.println(group);
        }
    }

    private void removeGroup() {

        System.out.print("Group ID: ");

        String id =
                scanner.nextLine();

        groupService.removeGroup(id);
    }
}