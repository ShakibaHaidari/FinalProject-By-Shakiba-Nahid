let api = "http://localhost:8080";

let settingsBtn =
    document.getElementById("settingsBtn");

let newChatBtn =
    document.getElementById("newChatBtn");

let search =
    document.getElementById("search");

let chatList =
    document.getElementById("chatList");

let welcome =
    document.getElementById("welcome");

let homeMessage =
    document.getElementById("homeMessage");

let userId =
    localStorage.getItem("userId");

let username =
    localStorage.getItem("username");

let loadingChats = false;


if (userId === null) {

    userId =
        sessionStorage.getItem("userId");

    username =
        sessionStorage.getItem("username");
}


if (userId === null) {

    window.location.href =
        "Login.html";

} else {

    if (username !== null) {

        welcome.innerText =
            "Welcome " + username;
    }

    loadChats();

    setInterval(
        loadChats,
        5000
    );
}


settingsBtn.addEventListener(
    "click",
    function (event) {

        event.preventDefault();

        window.location.href =
            "Setting.html";
    }
);


newChatBtn.addEventListener(
    "click",
    function (event) {

        event.preventDefault();

        window.location.href =
            "NewChat.html";
    }
);


search.addEventListener(
    "input",
    searchChats
);


async function loadChats() {

    if (loadingChats === true) {
        return;
    }

    loadingChats = true;

    homeMessage.innerText = "";

    try {

        let usersResponse =
            await fetch(
                api + "/api/users"
            );

        let users =
            await usersResponse.json();


        let groupsResponse =
            await fetch(
                api + "/api/groups"
            );

        let groups =
            await groupsResponse.json();


        let settingsResponse =
            await fetch(
                api
                + "/api/chats/settings?userId="
                + encodeURIComponent(userId)
            );

        let settings =
            await settingsResponse.json();


        if (Array.isArray(users) === false
                || Array.isArray(groups) === false
                || Array.isArray(settings) === false) {

            throw new Error(
                "Invalid server response"
            );
        }


        let settingMap = {};

        for (let i = 0;
             i < settings.length;
             i++) {

            settingMap[
                settings[i].chatId
            ] = settings[i];
        }


        let chats = [];


        for (let i = 0;
             i < users.length;
             i++) {

            if (String(users[i].id)
                    === String(userId)) {

                continue;
            }


            let personalId =
                makePersonalChatId(
                    userId,
                    users[i].id
                );


            let personalSetting =
                settingMap[personalId];


            if (personalSetting !== undefined
                    && personalSetting.archived === true) {

                continue;
            }


            let personalChat = {

                id:
                    personalId,

                name:
                    users[i].username,

                type:
                    "user",

                profileUserId:
                    users[i].id,

                fallbackText:
                    "Personal chat",

                text:
                    "Personal chat",

                pinned:
                    personalSetting !== undefined
                    && personalSetting.pinned === true,

                lastTime:
                    0,

                timeText:
                    "",

                unreadCount:
                    0,

                hasMessages:
                    false
            };


            await loadChatSummary(
                personalChat
            );


            if (personalChat.hasMessages === true
                    || personalChat.pinned === true) {

                chats.push(
                    personalChat
                );
            }
        }


        for (let i = 0;
             i < groups.length;
             i++) {

            let groupInfo =
                await getGroupInformation(
                    groups[i].id
                );


            if (groupInfo !== null
                    && groupInfo.currentUserIsMember === false) {

                continue;
            }


            let groupSetting =
                settingMap[
                    groups[i].id
                ];


            if (groupSetting !== undefined
                    && groupSetting.archived === true) {

                continue;
            }


            let memberCount =
                groups[i].memberCount;


            if (memberCount === undefined
                    || memberCount === null) {

                if (groupInfo !== null
                        && Array.isArray(
                            groupInfo.members
                        )) {

                    memberCount =
                        groupInfo.members.length;

                } else {

                    memberCount =
                        0;
                }
            }


            let groupChat = {

                id:
                    groups[i].id,

                name:
                    groups[i].name,

                type:
                    "group",

                profileUserId:
                    null,

                fallbackText:
                    memberCount
                    + " members",

                text:
                    memberCount
                    + " members",

                pinned:
                    groupSetting !== undefined
                    && groupSetting.pinned === true,

                lastTime:
                    0,

                timeText:
                    "",

                unreadCount:
                    0,

                hasMessages:
                    false
            };


            await loadChatSummary(
                groupChat
            );


            chats.push(
                groupChat
            );
        }


        chats.sort(
            function (first, second) {

                if (first.pinned === true
                        && second.pinned === false) {

                    return -1;
                }


                if (first.pinned === false
                        && second.pinned === true) {

                    return 1;
                }


                if (first.lastTime
                        !== second.lastTime) {

                    return second.lastTime
                        - first.lastTime;
                }


                return first.name
                    .localeCompare(
                        second.name
                    );
            }
        );


        chatList.innerHTML = "";


        if (chats.length === 0) {

            chatList.innerHTML =
                '<p class="empty">'
                + 'No active chats found'
                + '</p>';

            return;
        }


        for (let i = 0;
             i < chats.length;
             i++) {

            addChat(
                chats[i]
            );
        }


        searchChats();


    } catch (error) {

        chatList.innerHTML =
            '<p class="empty">'
            + 'Server is not connected'
            + '</p>';

    } finally {

        loadingChats = false;
    }
}


async function loadChatSummary(
    chatData
) {

    try {

        let response =
            await fetch(
                api
                + "/api/messages?chatId="
                + encodeURIComponent(
                    chatData.id
                )
            );


        let chatMessages =
            await response.json();


        if (Array.isArray(chatMessages) === false
                || chatMessages.length === 0) {

            chatData.hasMessages =
                false;

            return;
        }


        chatData.hasMessages =
            true;


        let lastMessage =
            chatMessages[0];


        for (let i = 1;
             i < chatMessages.length;
             i++) {

            if (getMessageTime(
                    chatMessages[i]
            ) > getMessageTime(
                    lastMessage
            )) {

                lastMessage =
                    chatMessages[i];
            }
        }


        chatData.lastTime =
            getMessageTime(
                lastMessage
            );


        chatData.timeText =
            formatChatTime(
                chatData.lastTime
            );


        chatData.text =
            createLastMessageText(
                lastMessage,
                chatData.type
            );


        chatData.unreadCount =
            countUnreadMessages(
                chatData.id,
                chatMessages
            );


    } catch (error) {

        chatData.hasMessages =
            false;

        chatData.text =
            chatData.fallbackText;
    }
}


async function getGroupInformation(
    groupId
) {

    try {

        let response =
            await fetch(
                api
                + "/api/chats/info?type=group"
                + "&userId="
                + encodeURIComponent(userId)
                + "&chatId="
                + encodeURIComponent(groupId)
            );


        return await response.json();


    } catch (error) {

        return null;
    }
}


function createLastMessageText(
    lastMessage,
    chatType
) {

    if (lastMessage.deleted === true) {

        return "Deleted message";
    }


    let content =
        lastMessage.content;


    if (content === null
            || content === undefined) {

        content = "";
    }


    if (content.startsWith(
            "__MEDIA__|"
    )) {

        let parts =
            content.split("|");


        if (parts.length === 4) {

            content =
                "📎 " + parts[2];

        } else {

            content =
                "📎 Media file";
        }
    }


    if (String(lastMessage.senderId)
            === String(userId)) {

        content =
            "You: " + content;

    } else if (chatType === "group") {

        content =
            lastMessage.senderId
            + ": "
            + content;
    }


    if (content.length > 48) {

        content =
            content.substring(
                0,
                48
            )
            + "...";
    }


    return content;
}


function getMessageTime(
    messageData
) {

    if (messageData.createdAt === null
            || messageData.createdAt === undefined) {

        return 0;
    }


    let time =
        new Date(
            messageData.createdAt
        ).getTime();


    if (Number.isNaN(time)) {

        return 0;
    }


    return time;
}


function formatChatTime(
    time
) {

    if (time === 0) {

        return "";
    }


    let date =
        new Date(time);

    let today =
        new Date();


    if (date.toDateString()
            === today.toDateString()) {

        return date.toLocaleTimeString(
            [],
            {
                hour:
                    "2-digit",

                minute:
                    "2-digit"
            }
        );
    }


    return date.toLocaleDateString();
}


function countUnreadMessages(
    chatId,
    chatMessages
) {

    let key =
        createReadKey(
            chatId
        );


    let savedTime =
        localStorage.getItem(key);


    if (savedTime === null) {

        localStorage.setItem(
            key,
            String(Date.now())
        );

        return 0;
    }


    let lastReadTime =
        Number(savedTime);

    let count = 0;


    for (let i = 0;
         i < chatMessages.length;
         i++) {

        if (String(chatMessages[i].senderId)
                === String(userId)) {

            continue;
        }


        if (chatMessages[i].deleted === true) {

            continue;
        }


        if (getMessageTime(
                chatMessages[i]
        ) > lastReadTime) {

            count++;
        }
    }


    return count;
}


function createReadKey(
    chatId
) {

    return "chat_last_read_"
        + userId
        + "_"
        + chatId;
}


function markChatAsRead(
    chatId
) {

    localStorage.setItem(
        createReadKey(chatId),
        String(Date.now())
    );
}


function searchChats() {

    let text =
        search.value
            .trim()
            .toLowerCase();


    let chats =
        document.getElementsByClassName(
            "chat"
        );


    for (let i = 0;
         i < chats.length;
         i++) {

        let name =
            chats[i].getAttribute(
                "data-name"
            );

        let id =
            chats[i].getAttribute(
                "data-id"
            );

        let detail =
            chats[i].getAttribute(
                "data-text"
            );


        if (name.includes(text)
                || id.includes(text)
                || detail.includes(text)) {

            chats[i].style.display =
                "flex";

        } else {

            chats[i].style.display =
                "none";
        }
    }
}


function addChat(
    chatData
) {

    let chat =
        document.createElement("div");

    chat.className =
        "chat";


    chat.setAttribute(
        "data-name",
        chatData.name.toLowerCase()
    );


    chat.setAttribute(
        "data-id",
        chatData.id.toLowerCase()
    );


    chat.setAttribute(
        "data-text",
        chatData.text.toLowerCase()
    );


    let main =
        document.createElement("div");

    main.className =
        "chat-main";


    let profile =
        document.createElement("div");

    profile.className =
        "profile";


    profile.innerText =
        chatData.name
            .charAt(0)
            .toUpperCase();


    if (chatData.type === "user"
            && chatData.profileUserId !== null) {

        loadSmallProfilePicture(
            profile,
            chatData.profileUserId
        );

    } else if (chatData.type === "group") {

        loadSmallGroupPicture(
            profile,
            chatData.id
        );
    }


    let info =
        document.createElement("div");

    info.className =
        "chat-info";


    let titleRow =
        document.createElement("div");

    titleRow.className =
        "chat-title-row";


    let title =
        document.createElement("h3");


    if (chatData.pinned === true) {

        title.innerText =
            "📌 " + chatData.name;

    } else {

        title.innerText =
            chatData.name;
    }


    let time =
        document.createElement("span");

    time.className =
        "chat-time";

    time.innerText =
        chatData.timeText;


    titleRow.appendChild(
        title
    );

    titleRow.appendChild(
        time
    );


    let detailRow =
        document.createElement("div");

    detailRow.className =
        "chat-detail-row";


    let detail =
        document.createElement("p");

    detail.innerText =
        chatData.text;


    detailRow.appendChild(
        detail
    );


    if (chatData.unreadCount > 0) {

        let unread =
            document.createElement("span");

        unread.className =
            "unread-count";


        if (chatData.unreadCount > 99) {

            unread.innerText =
                "99+";

        } else {

            unread.innerText =
                chatData.unreadCount;
        }


        detailRow.appendChild(
            unread
        );
    }


    info.appendChild(
        titleRow
    );

    info.appendChild(
        detailRow
    );


    main.appendChild(
        profile
    );

    main.appendChild(
        info
    );


    main.addEventListener(
        "click",
        function () {

            openChat(
                chatData
            );
        }
    );


    let actions =
        document.createElement("div");

    actions.className =
        "chat-actions";


    let pinBtn =
        document.createElement("button");


    if (chatData.pinned === true) {

        pinBtn.innerText =
            "Unpin";

    } else {

        pinBtn.innerText =
            "Pin";
    }


    pinBtn.addEventListener(
        "click",
        function () {

            changePin(
                chatData.id,
                chatData.pinned === false
            );
        }
    );


    let archiveBtn =
        document.createElement("button");

    archiveBtn.innerText =
        "Archive";


    archiveBtn.addEventListener(
        "click",
        function () {

            archiveChat(
                chatData.id
            );
        }
    );


    actions.appendChild(
        pinBtn
    );

    actions.appendChild(
        archiveBtn
    );


    chat.appendChild(
        main
    );

    chat.appendChild(
        actions
    );


    chatList.appendChild(
        chat
    );
}


function openChat(
    chatData
) {

    markChatAsRead(
        chatData.id
    );


    window.location.href =
        "Chat.html?chatId="
        + encodeURIComponent(
            chatData.id
        )
        + "&name="
        + encodeURIComponent(
            chatData.name
        )
        + "&type="
        + encodeURIComponent(
            chatData.type
        );
}


async function archiveChat(
    chatId
) {

    let answer =
        confirm(
            "Do you want to archive this chat?"
        );


    if (answer === false) {

        return;
    }


    let body =
        "userId="
        + encodeURIComponent(userId)
        + "&chatId="
        + encodeURIComponent(chatId)
        + "&archived=true";


    let data =
        await postSetting(
            api + "/api/chats/archive",
            body
        );


    if (data !== null) {

        showHomeResult(data);


        if (data.success === true) {

            loadChats();
        }
    }
}


async function changePin(
    chatId,
    pinned
) {

    let body =
        "userId="
        + encodeURIComponent(userId)
        + "&chatId="
        + encodeURIComponent(chatId)
        + "&pinned="
        + encodeURIComponent(pinned);


    let data =
        await postSetting(
            api + "/api/chats/pin",
            body
        );


    if (data !== null) {

        showHomeResult(data);


        if (data.success === true) {

            loadChats();
        }
    }
}


async function postSetting(
    url,
    body
) {

    try {

        let response =
            await fetch(
                url,
                {
                    method:
                        "POST",

                    headers: {

                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body:
                        body
                }
            );


        return await response.json();


    } catch (error) {

        homeMessage.style.color =
            "red";

        homeMessage.innerText =
            "Server is not connected";

        return null;
    }
}


function showHomeResult(
    data
) {

    if (data.success === true) {

        homeMessage.style.color =
            "green";

    } else {

        homeMessage.style.color =
            "red";
    }


    homeMessage.innerText =
        data.message;
}


function makePersonalChatId(
    firstId,
    secondId
) {

    if (String(firstId)
            < String(secondId)) {

        return "p_"
            + firstId
            + "_"
            + secondId;
    }


    return "p_"
        + secondId
        + "_"
        + firstId;
}


async function loadSmallProfilePicture(
    profileElement,
    profileUserId
) {

    try {

        let response =
            await fetch(
                api
                + "/api/account/profile-picture?userId="
                + encodeURIComponent(
                    profileUserId
                )
            );


        let data =
            await response.json();


        if (data.success === true
                && data.imageData !== "") {

            profileElement.innerText =
                "";

            profileElement.style.backgroundImage =
                "url('"
                + data.imageData
                + "')";
        }


    } catch (error) {

        console.log(
            "Profile picture could not be loaded"
        );
    }
}


async function loadSmallGroupPicture(
    profileElement,
    groupId
) {

    try {

        let response =
            await fetch(
                api
                + "/api/groups/profile-picture?groupId="
                + encodeURIComponent(groupId)
            );


        let data =
            await response.json();


        if (data.success === true
                && data.imageData !== "") {

            profileElement.innerText =
                "";

            profileElement.style.backgroundImage =
                "url('"
                + data.imageData
                + "')";
        }


    } catch (error) {

        console.log(
            "Group picture could not be loaded"
        );
    }
}