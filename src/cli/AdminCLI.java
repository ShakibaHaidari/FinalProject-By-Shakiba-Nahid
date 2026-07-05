package cli;

import model.Group;
import model.Message;
import model.User;
import service.GroupService;
import service.UserService;

import java.util.List;
import java.util.Scanner;

public class AdminCLI {

    private static final String ADMIN_USERNAME =
            "admin";

    private static final String ADMIN_PASSWORD =
            "Moha@123";

    private final Scanner scanner =
            new Scanner(System.in);

    private final UserService userService;
    private final GroupService groupService;

    public AdminCLI(
            UserService userService,
            GroupService groupService) {

        this.userService = userService;
        this.groupService = groupService;
    }

    public void start() {

        System.out.println(
                "\n===== CHAT WEB ADMIN CLI ====="
        );

        if (!adminLogin()) {

            System.out.println(
                    "Too many failed attempts. CLI closed."
            );

            return;
        }

        boolean running = true;

        while (running) {

            printMenu();

            int choice =
                    readInt("Choose an option: ");

            switch (choice) {

                case 1 -> addUser();
                case 2 -> listUsers();
                case 3 -> removeUser();

                case 4 -> addGroup();
                case 5 -> listGroups();
                case 6 -> removeGroup();

                case 7 -> addUserToGroup();
                case 8 -> removeUserFromGroup();
                case 9 -> showGroupMembers();

                case 10 -> showReportedMessages();

                case 11 -> editUserUsername();
                case 12 -> changeUserPassword();
                case 13 -> editGroupName();

                case 0 -> {

                    running = false;

                    System.out.println(
                            "Admin CLI closed."
                    );
                }

                default -> System.out.println(
                        "Invalid option. Please try again."
                );
            }
        }
    }

    private boolean adminLogin() {

        for (int attempt = 1;
             attempt <= 3;
             attempt++) {

            String username =
                    readNonBlank("Admin username: ");

            String password =
                    readNonBlank("Admin password: ");

            if (ADMIN_USERNAME.equals(username)
                    && ADMIN_PASSWORD.equals(password)) {

                System.out.println(
                        "Admin login successful.\n"
                );

                return true;
            }

            System.out.println(
                    "Invalid admin credentials. Attempt "
                            + attempt
                            + " of 3."
            );
        }

        return false;
    }

    private void printMenu() {

        System.out.println("""
                
                ===== ADMIN MENU =====
                1. Add User
                2. List Users
                3. Remove User
                4. Add Group
                5. List Groups
                6. Remove Group
                7. Add User To Group
                8. Remove User From Group
                9. Show Group Members
                10. Show Reported Messages
                11. Edit User Username
                12. Change User Password
                13. Edit Group Name
                0. Exit
                """);
    }

    private void addUser() {

        String id =
                readNonBlank("User ID: ");

        String username =
                readNonBlank("Username: ");

        String password =
                readNonBlank("Password: ");

        UserService.RegistrationResult result =
                userService.register(
                        new User(
                                id,
                                username,
                                password
                        )
                );

        switch (result) {

            case SUCCESS -> System.out.println(
                    "User added successfully."
            );

            case ID_EXISTS -> System.out.println(
                    "This user ID already exists."
            );

            case USERNAME_EXISTS -> System.out.println(
                    "This username already exists."
            );

            case INVALID_PASSWORD -> System.out.println(
                    "Invalid password. Use 8+ characters with uppercase, lowercase, number and !@%$#^&*."
            );

            case INVALID_INPUT -> System.out.println(
                    "Invalid user information."
            );
        }
    }

    private void listUsers() {

        List<User> users =
                userService.getUsers();

        if (users.isEmpty()) {

            System.out.println(
                    "No users found."
            );

            return;
        }

        System.out.println("\n--- USERS ---");

        for (User user : users) {
            System.out.println(user);
        }
    }

    private void removeUser() {

        String userId =
                readNonBlank("User ID to remove: ");

        if (userService.removeUser(userId)) {

            groupService.removeUserFromAllGroups(
                    userId
            );

            System.out.println(
                    "User removed successfully."
            );

        } else {

            System.out.println(
                    "User not found."
            );
        }
    }

    private void addGroup() {

        String id =
                readNonBlank("Group ID: ");

        String name =
                readNonBlank("Group name: ");

        if (groupService.addGroup(
                new Group(id, name))) {

            System.out.println(
                    "Group added successfully."
            );

        } else {

            System.out.println(
                    "Group could not be added. Check if the group ID is unique."
            );
        }
    }

    private void listGroups() {

        List<Group> groups =
                groupService.getGroups();

        if (groups.isEmpty()) {

            System.out.println(
                    "No groups found."
            );

            return;
        }

        System.out.println("\n--- GROUPS ---");

        for (Group group : groups) {
            System.out.println(group);
        }
    }

    private void removeGroup() {

        String groupId =
                readNonBlank("Group ID to remove: ");

        if (groupService.removeGroup(groupId)) {

            System.out.println(
                    "Group removed successfully."
            );

        } else {

            System.out.println(
                    "Group not found."
            );
        }
    }

    private void addUserToGroup() {

        String groupId =
                readNonBlank("Group ID: ");

        String userId =
                readNonBlank("User ID: ");

        if (userService.getUserById(userId) == null) {

            System.out.println(
                    "User not found."
            );

            return;
        }

        if (groupService.getGroupById(groupId)
                == null) {

            System.out.println(
                    "Group not found."
            );

            return;
        }

        if (groupService.addUserToGroup(
                groupId,
                userId)) {

            System.out.println(
                    "User added to group successfully."
            );

        } else {

            System.out.println(
                    "User is already a member of this group."
            );
        }
    }

    private void removeUserFromGroup() {

        String groupId =
                readNonBlank("Group ID: ");

        String userId =
                readNonBlank("User ID: ");

        if (groupService.removeUserFromGroup(
                groupId,
                userId)) {

            System.out.println(
                    "User removed from group successfully."
            );

        } else {

            System.out.println(
                    "Group not found or user is not a member."
            );
        }
    }

    private void showGroupMembers() {

        String groupId =
                readNonBlank("Group ID: ");

        Group group =
                groupService.getGroupById(groupId);

        if (group == null) {

            System.out.println(
                    "Group not found."
            );

            return;
        }

        List<String> memberIds =
                groupService.getGroupMembers(groupId);

        if (memberIds.isEmpty()) {

            System.out.println(
                    "This group has no members."
            );

            return;
        }

        System.out.println(
                "\n--- MEMBERS OF "
                        + group.getName()
                        + " ---"
        );

        for (String userId : memberIds) {

            User user =
                    userService.getUserById(userId);

            if (user == null) {

                System.out.println(
                        "User ID: " + userId
                );

            } else {

                System.out.println(
                        "ID: "
                                + user.getId()
                                + " | Username: "
                                + user.getUsername()
                );
            }
        }
    }

    private void showReportedMessages() {

        List<Message> reportedMessages =
                groupService.getReportedMessages();

        if (reportedMessages.isEmpty()) {

            System.out.println(
                    "No reported messages found."
            );

            return;
        }

        System.out.println(
                "\n--- REPORTED MESSAGES ---"
        );

        for (Message message : reportedMessages) {

            User sender =
                    userService.getUserById(
                            message.getSenderId()
                    );

            String senderName =
                    sender == null
                            ? "Unknown user"
                            : sender.getUsername();

            System.out.println(
                    "Sender: "
                            + senderName
                            + " ("
                            + message.getSenderId()
                            + ")"
            );

            System.out.println(
                    "Message: "
                            + message.getContent()
            );

            System.out.println(
                    "Date: "
                            + message.getCreatedAt()
            );

            System.out.println(
                    "-------------------------"
            );
        }
    }

    private void editUserUsername() {

        String userId =
                readNonBlank("Enter User ID: ");

        String newUsername =
                readNonBlank("Enter New Username: ");

        UserService.UpdateUsernameResult result =
                userService.updateUsername(
                        userId,
                        newUsername
                );

        switch (result) {

            case SUCCESS -> System.out.println(
                    "Username updated successfully."
            );

            case USER_NOT_FOUND -> System.out.println(
                    "User not found."
            );

            case USERNAME_EXISTS -> System.out.println(
                    "This username already exists."
            );

            case INVALID_USERNAME -> System.out.println(
                    "Username cannot be empty."
            );
        }
    }

    private void changeUserPassword() {

        String userId =
                readNonBlank("Enter User ID: ");

        String newPassword =
                readNonBlank("Enter New Password: ");

        UserService.ChangePasswordResult result =
                userService.changePassword(
                        userId,
                        newPassword
                );

        switch (result) {

            case SUCCESS -> System.out.println(
                    "Password changed successfully."
            );

            case USER_NOT_FOUND -> System.out.println(
                    "User not found."
            );

            case INVALID_PASSWORD -> System.out.println(
                    "Invalid password. Use 8+ characters with uppercase, lowercase, number and !@%$#^&*."
            );
        }
    }

    private void editGroupName() {

        String groupId =
                readNonBlank("Enter Group ID: ");

        String newGroupName =
                readNonBlank("Enter New Group Name: ");

        GroupService.UpdateGroupResult result =
                groupService.updateGroupName(
                        groupId,
                        newGroupName
                );

        switch (result) {

            case SUCCESS -> System.out.println(
                    "Group name updated successfully."
            );

            case GROUP_NOT_FOUND -> System.out.println(
                    "Group not found."
            );

            case INVALID_GROUP_NAME -> System.out.println(
                    "Group name cannot be empty."
            );
        }
    }

    private int readInt(String message) {

        while (true) {

            System.out.print(message);

            String value =
                    scanner.nextLine();

            try {

                return Integer.parseInt(value);

            } catch (NumberFormatException exception) {

                System.out.println(
                        "Please enter a valid number."
                );
            }
        }
    }

    private String readNonBlank(String message) {

        while (true) {

            System.out.print(message);

            String value =
                    scanner.nextLine().trim();

            if (!value.isBlank()) {
                return value;
            }

            System.out.println(
                    "This value cannot be empty."
            );
        }
    }
}