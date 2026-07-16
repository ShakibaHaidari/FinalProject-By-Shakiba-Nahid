
package service;

import model.BlockUser;
import repository.BlockedUserRepository;
import java.util.ArrayList;
import java.util.List;
public class BlockedUserService {
    private final List<BlockUser> blockedUsers;
    private final BlockedUserRepository blockedUserRepository;

    public BlockedUserService() {

        this.blockedUserRepository = new BlockedUserRepository();

        this.blockedUsers = new ArrayList<>(blockedUserRepository.loadAll());

        System.out.println(
                "Loaded "
                        + blockedUsers.size()
                        + " blocked user(s) from blocked_users.txt."
        );
    }

    public synchronized boolean blockUser(
            String userId,
            String blockedUserId
    ) {
        if (isBlank(userId) || isBlank(blockedUserId)) {
            return false;
        }

        if (userId.equalsIgnoreCase(blockedUserId)) {
            return false;
        }

        if (isBlocked(userId, blockedUserId)) {
            return false;
        }

        BlockUser blockedUser =
                new BlockUser(
                        userId,
                        blockedUserId
                );

        blockedUsers.add(blockedUser);

        persistBlockedUsers();

        return true;
    }

    public synchronized boolean unblockUser(
            String userId,
            String blockedUserId
    ) {
        BlockUser found = null;

        for (BlockUser blockedUser : blockedUsers) {

            boolean sameUser =
                    blockedUser
                            .getUserId()
                            .equalsIgnoreCase(userId);

            boolean sameBlockedUser =
                    blockedUser
                            .getBlockedUserId()
                            .equalsIgnoreCase(blockedUserId);

            if (sameUser && sameBlockedUser) {
                found = blockedUser;
                break;
            }
        }

        if (found == null) {
            return false;
        }

        blockedUsers.remove(found);

        persistBlockedUsers();

        return true;
    }

    public synchronized List<BlockUser> getBlockedUsers(
            String userId
    ) {
        List<BlockUser> result =
                new ArrayList<>();

        for (BlockUser blockedUser : blockedUsers) {

            if (blockedUser
                    .getUserId()
                    .equalsIgnoreCase(userId)) {

                result.add(blockedUser);
            }
        }

        return result;
    }

    public synchronized boolean isBlocked(
            String userId,
            String blockedUserId
    ) {
        for (BlockUser blockedUser : blockedUsers) {

            boolean sameUser =
                    blockedUser
                            .getUserId()
                            .equalsIgnoreCase(userId);

            boolean sameBlockedUser =
                    blockedUser
                            .getBlockedUserId()
                            .equalsIgnoreCase(blockedUserId);

            if (sameUser && sameBlockedUser) {
                return true;
            }
        }
        return false;
    }
    private void persistBlockedUsers() {
        blockedUserRepository.saveAll(blockedUsers);
    }
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}