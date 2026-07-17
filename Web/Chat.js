let api = "http://localhost:8080";

let chatName = document.getElementById("chatName");
let chatStatus = document.getElementById("chatStatus");
let historyBtn = document.getElementById("historyBtn");
let messages = document.getElementById("messages");
let message = document.getElementById("message");
let sendButton = document.getElementById("sendButton");
let messageStatus = document.getElementById("messageStatus");
let chatProfile = document.getElementById("chatProfile");
let chatInfoLink = document.getElementById("chatInfoLink");
let messageSearch = document.getElementById("messageSearch");
let clearSearchBtn = document.getElementById("clearSearchBtn");
let searchResult = document.getElementById("searchResult");

let address = new URLSearchParams(window.location.search);

let chatId = address.get("chatId");
let name = address.get("name");
let type = address.get("type");

let userId = localStorage.getItem("userId");
let username = localStorage.getItem("username");

let socket = null;
let reconnectTimer = null;
let sendTimer = null;
let pageIsClosing = false;


if (userId === null) {

    userId =
        sessionStorage.getItem("userId");

    username =
        sessionStorage.getItem("username");
}


if (chatId === null) {

    chatId = "g1";
}


if (name === null) {

    name = "Group Chat";
}


chatName.innerText = name;


if (type === "group") {

    chatStatus.innerText =
        "Group chat";

} else {

    chatStatus.innerText =
        "Personal chat";
}


if (userId === null) {

    chatStatus.innerText =
        "Please log in first";

    messageStatus.innerText =
        "No logged-in user found";

    message.disabled = true;

    sendButton.disabled = true;

} else {

    connectWebSocket();

    updateMyActivity();

    loadChatProfilePicture();

    loadChatHeaderInformation();

    loadMessages();


    setInterval(
        function () {

            updateMyActivity();

            if (type === "user") {

                loadChatHeaderInformation();
            }
        },

        30000
    );


    setInterval(
        function () {

            if (socket === null
                    || socket.readyState
                    !== WebSocket.OPEN) {

                loadMessages();
            }
        },

        3000
    );
}


async function loadMessages() {

    try {

        let response =
            await fetch(
                api
                + "/api/messages?chatId="
                + encodeURIComponent(chatId)
            );


        if (response.ok === false) {

            messageStatus.style.color =
                "red";

            messageStatus.innerText =
                "Cannot load messages";

            return;
        }


        let data =
            await response.json();
            markCurrentChatAsRead();


        if (Array.isArray(data) === false) {

            messageStatus.style.color =
                "red";

            messageStatus.innerText =
                "Cannot load messages";

            return;
        }


        messages.innerHTML = "";


        if (data.length === 0) {

            messages.innerHTML =
                '<p class="loading">'
                + 'No messages yet'
                + '</p>';


            if (searchResult !== null) {

                searchResult.innerText = "";
            }

            return;
        }


        for (let i = 0;
             i < data.length;
             i++) {

            showMessage(data[i]);
        }


        searchMessages();


        messages.scrollTop =
            messages.scrollHeight;


    } catch (error) {

        messageStatus.style.color =
            "red";

        messageStatus.innerText =
            "Server is not connected";
    }
}


async function sendMessage() {

    let text =
        message.value.trim();


    if (text === "") {

        messageStatus.style.color =
            "red";

        messageStatus.innerText =
            "Please write a message";

        return;
    }


    if (text.length > 500) {

        messageStatus.style.color =
            "red";

        messageStatus.innerText =
            "Message cannot be more than 500 characters";

        return;
    }


    if (userId === null) {

        messageStatus.style.color =
            "red";

        messageStatus.innerText =
            "Please log in first";

        return;
    }


    let body =
        "chatId="
        + encodeURIComponent(chatId)

        + "&senderId="
        + encodeURIComponent(userId)

        + "&content="
        + encodeURIComponent(text);


    if (socket !== null
            && socket.readyState
            === WebSocket.OPEN) {

        sendButton.disabled =
            true;

        sendButton.innerText =
            "Wait...";

        messageStatus.style.color =
            "#777777";

        messageStatus.innerText =
            "Sending...";


        try {

            socket.send(body);


            clearSendTimer();


            sendTimer =
                setTimeout(
                    function () {

                        sendButton.disabled =
                            false;

                        sendButton.innerText =
                            "Send";


                        if (messageStatus.innerText
                                === "Sending...") {

                            messageStatus.style.color =
                                "red";

                            messageStatus.innerText =
                                "Message response was not received";
                        }
                    },

                    5000
                );


        } catch (error) {

            clearSendTimer();

            await sendMessageWithRest(
                body
            );
        }

        return;
    }


    await sendMessageWithRest(
        body
    );
}


async function sendMessageWithRest(
    body
) {

    sendButton.disabled =
        true;

    sendButton.innerText =
        "Wait...";


    try {

        let response =
            await fetch(
                api + "/api/messages",

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


        let data =
            await response.json();


        if (data.success === true) {

            message.value = "";

            messageStatus.innerText = "";

            loadMessages();

        } else {

            messageStatus.style.color =
                "red";

            messageStatus.innerText =
                data.message;
        }


    } catch (error) {

        messageStatus.style.color =
            "red";

        messageStatus.innerText =
            "Cannot send message";

    } finally {

        sendButton.disabled =
            false;

        sendButton.innerText =
            "Send";
    }
}


function showMessage(data) {

    let mine =
        String(data.senderId)
        === String(userId);


    let box =
        document.createElement("div");
        box.dataset.messageId =
            data.id;

        box.dataset.mine =
            mine === true
                ? "true"
                : "false";



    if (mine === true) {

        box.className =
            "message my-message";

    } else {

        box.className =
            "message other-message";
    }


    let text =
        document.createElement("p");

    text.className =
        "message-text";

    text.innerText =
        data.content;


    let info =
        document.createElement("p");

    info.className =
        "message-info";


    if (mine === true) {

        info.innerText =
            "You";

    } else {

        info.innerText =
            data.senderId;
    }


    if (data.createdAt !== undefined
            && data.createdAt !== null) {

        let date =
            data.createdAt
                .replace("T", " ")
                .substring(0, 16);

        info.innerText +=
            " - " + date;
    }


    if (data.edited === true) {

        info.innerText +=
            " - Edited";
    }


    if (data.deleted === true) {

        info.innerText +=
            " - Deleted";
    }


    box.appendChild(text);

    box.appendChild(info);


    if (data.deleted !== true) {

        let buttons =
            document.createElement("div");

        buttons.className =
            "message-buttons";


        let saveBtn =
            document.createElement("button");

        saveBtn.innerText =
            "Save";


        saveBtn.addEventListener(
            "click",
            function () {

                saveMessage(
                    data.id
                );
            }
        );


        buttons.appendChild(
            saveBtn
        );


        if (mine === true) {

            let editBtn =
                document.createElement(
                    "button"
                );

            editBtn.innerText =
                "Edit";


            editBtn.addEventListener(
                "click",
                function () {

                    editMessage(
                        data.id,
                        data.content
                    );
                }
            );


            let deleteBtn =
                document.createElement(
                    "button"
                );

            deleteBtn.innerText =
                "Delete";


            deleteBtn.addEventListener(
                "click",
                function () {

                    deleteMessage(
                        data.id
                    );
                }
            );


            buttons.appendChild(
                editBtn
            );

            buttons.appendChild(
                deleteBtn
            );

        } else {

            let reportBtn =
                document.createElement(
                    "button"
                );

            reportBtn.innerText =
                "Report";


            reportBtn.addEventListener(
                "click",
                function () {

                    reportMessage(
                        data.id
                    );
                }
            );


            buttons.appendChild(
                reportBtn
            );
        }


        box.appendChild(
            buttons
        );
    }


    messages.appendChild(
        box
    );
}


async function editMessage(
    messageId,
    oldContent
) {

    let newContent =
        prompt(
            "Write the new message:",
            oldContent
        );


    if (newContent === null) {

        return;
    }


    newContent =
        newContent.trim();


    if (newContent === "") {

        messageStatus.style.color =
            "red";

        messageStatus.innerText =
            "Message cannot be empty";

        return;
    }


    if (newContent.length > 500) {

        messageStatus.style.color =
            "red";

        messageStatus.innerText =
            "Message cannot be more than 500 characters";

        return;
    }


    let body =
        "messageId="
        + encodeURIComponent(messageId)

        + "&senderId="
        + encodeURIComponent(userId)

        + "&newContent="
        + encodeURIComponent(newContent);


    let data =
        await postRequest(
            api + "/api/messages/edit",
            body
        );


    if (data !== null) {

        showResult(data);


        if (data.success === true) {

            loadMessages();
        }
    }
}


async function deleteMessage(
    messageId
) {

    let answer =
        confirm(
            "Do you want to delete this message?"
        );


    if (answer === false) {

        return;
    }


    let body =
        "messageId="
        + encodeURIComponent(messageId)

        + "&senderId="
        + encodeURIComponent(userId);


    let data =
        await postRequest(
            api + "/api/messages/delete",
            body
        );


    if (data !== null) {

        showResult(data);


        if (data.success === true) {

            loadMessages();
        }
    }
}


async function saveMessage(
    messageId
) {

    let body =
        "userId="
        + encodeURIComponent(userId)

        + "&messageId="
        + encodeURIComponent(messageId);


    let data =
        await postRequest(
            api + "/api/saved-messages",
            body
        );


    if (data !== null) {

        showResult(data);
    }
}


async function reportMessage(
    messageId
) {

    let answer =
        confirm(
            "Do you want to report this message?"
        );


    if (answer === false) {

        return;
    }


    let body =
        "messageId="
        + encodeURIComponent(messageId);


    let data =
        await postRequest(
            api + "/api/messages/report",
            body
        );


    if (data !== null) {

        showResult(data);
    }
}


async function postRequest(
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

        messageStatus.style.color =
            "red";

        messageStatus.innerText =
            "Server is not connected";

        return null;
    }
}


function showResult(data) {

    if (data.success === true) {

        messageStatus.style.color =
            "green";

    } else {

        messageStatus.style.color =
            "red";
    }


    messageStatus.innerText =
        data.message;
}


async function loadChatProfilePicture() {

    if (chatProfile === null) {

        return;
    }


    if (type === "group") {

        await loadGroupChatPicture(
            name
        );

        return;
    }


    if (type !== "user") {

        showChatProfileLetter(
            name
        );

        return;
    }


    try {

        let usersResponse =
            await fetch(
                api + "/api/users"
            );


        let users =
            await usersResponse.json();


        let targetUser =
            null;


        if (Array.isArray(users)
                === false) {

            showChatProfileLetter(
                name
            );

            return;
        }


        for (let i = 0;
             i < users.length;
             i++) {

            if (String(users[i].id)
                    === String(userId)) {

                continue;
            }


            let personalChatId =
                makeChatProfileId(
                    userId,
                    users[i].id
                );


            if (String(personalChatId)
                    === String(chatId)) {

                targetUser =
                    users[i];

                break;
            }
        }


        if (targetUser === null) {

            showChatProfileLetter(
                name
            );

            return;
        }


        let pictureResponse =
            await fetch(
                api
                + "/api/account/profile-picture?userId="
                + encodeURIComponent(
                    targetUser.id
                )
            );


        let pictureData =
            await pictureResponse.json();


        if (pictureData.success === true
                && pictureData.imageData !== "") {

            chatProfile.innerText =
                "";

            chatProfile.style.backgroundImage =
                "url('"
                + pictureData.imageData
                + "')";

        } else {

            showChatProfileLetter(
                targetUser.username
            );
        }


    } catch (error) {

        showChatProfileLetter(
            name
        );
    }
}


async function loadGroupChatPicture(
    groupName
) {

    try {

        let response =
            await fetch(
                api
                + "/api/groups/profile-picture?groupId="
                + encodeURIComponent(chatId)
            );


        let data =
            await response.json();


        if (data.success === true
                && data.imageData !== "") {

            chatProfile.innerText =
                "";

            chatProfile.style.backgroundImage =
                "url('"
                + data.imageData
                + "')";

        } else {

            showChatProfileLetter(
                groupName
            );
        }


    } catch (error) {

        showChatProfileLetter(
            groupName
        );
    }
}


function showChatProfileLetter(
    profileName
) {

    if (chatProfile === null) {

        return;
    }


    chatProfile.style.backgroundImage =
        "none";


    if (profileName === null
            || profileName === undefined
            || profileName === "") {

        chatProfile.innerText =
            "?";

        return;
    }


    chatProfile.innerText =
        profileName
            .charAt(0)
            .toUpperCase();
}


function makeChatProfileId(
    firstUserId,
    secondUserId
) {

    if (String(firstUserId)
            < String(secondUserId)) {

        return "p_"
            + firstUserId
            + "_"
            + secondUserId;
    }


    return "p_"
        + secondUserId
        + "_"
        + firstUserId;
}


function searchMessages() {

    if (messageSearch === null
            || searchResult === null) {

        return;
    }


    let searchText =
        messageSearch.value
            .trim()
            .toLowerCase();


    let messageBoxes =
        document.getElementsByClassName(
            "message"
        );


    let foundCount = 0;


    for (let i = 0;
         i < messageBoxes.length;
         i++) {

        let textElement =
            messageBoxes[i]
                .querySelector(
                    ".message-text"
                );


        let infoElement =
            messageBoxes[i]
                .querySelector(
                    ".message-info"
                );


        let messageText = "";

        let informationText = "";


        if (textElement !== null) {

            messageText =
                textElement.innerText
                    .toLowerCase();
        }


        if (infoElement !== null) {

            informationText =
                infoElement.innerText
                    .toLowerCase();
        }


        if (searchText === ""
                || messageText.includes(
                    searchText
                )
                || informationText.includes(
                    searchText
                )) {

            messageBoxes[i].style.display =
                "";

            foundCount++;

        } else {

            messageBoxes[i].style.display =
                "none";
        }
    }


    if (searchText === "") {

        searchResult.innerText = "";

        return;
    }


    if (foundCount === 0) {

        searchResult.innerText =
            "No matching messages found";

    } else {

        searchResult.innerText =
            foundCount
            + " matching message(s)";
    }
}


async function loadChatHeaderInformation() {

    if (type === "group") {

        await loadGroupMemberCount();

        return;
    }


    if (type === "user") {

        await loadPersonalLastSeen();

        return;
    }


    chatStatus.innerText =
        "Chat";
}


async function loadGroupMemberCount() {

    try {

        let response =
            await fetch(
                api
                + "/api/chats/info?type=group"

                + "&userId="
                + encodeURIComponent(userId)

                + "&chatId="
                + encodeURIComponent(chatId)
            );


        let data =
            await response.json();


        if (data.success === true) {

            chatStatus.innerText =
                data.memberCount
                + " members";

        } else {

            chatStatus.innerText =
                "Group chat";
        }


    } catch (error) {

        chatStatus.innerText =
            "Group chat";
    }
}


async function loadPersonalLastSeen() {

    try {

        let targetUserId =
            await findTargetUserIdForActivity();


        if (targetUserId === null) {

            chatStatus.innerText =
                "Personal chat";

            return;
        }


        let response =
            await fetch(
                api
                + "/api/users/activity?userId="
                + encodeURIComponent(
                    targetUserId
                )
            );


        let data =
            await response.json();


        if (data.success !== true
                || data.lastSeenMillis === 0) {

            chatStatus.innerText =
                "Last seen: unknown";

            return;
        }


        let difference =
            Date.now()
            - data.lastSeenMillis;


        if (difference <= 60000) {

            chatStatus.innerText =
                "Online";

            return;
        }


        let lastSeenDate =
            new Date(
                data.lastSeenMillis
            );


        chatStatus.innerText =
            "Last seen: "
            + lastSeenDate
                .toLocaleString();


    } catch (error) {

        chatStatus.innerText =
            "Personal chat";
    }
}


async function findTargetUserIdForActivity() {

    try {

        let response =
            await fetch(
                api + "/api/users"
            );


        let users =
            await response.json();


        if (Array.isArray(users)
                === false) {

            return null;
        }


        for (let i = 0;
             i < users.length;
             i++) {

            if (String(users[i].id)
                    === String(userId)) {

                continue;
            }


            let personalChatId =
                makeChatProfileId(
                    userId,
                    users[i].id
                );


            if (String(personalChatId)
                    === String(chatId)) {

                return users[i].id;
            }
        }


        return null;


    } catch (error) {

        return null;
    }
}


async function updateMyActivity() {

    let body =
        "userId="
        + encodeURIComponent(userId);


    try {

        await fetch(
            api
            + "/api/users/activity",

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


    } catch (error) {

        console.log(
            "User activity could not be updated"
        );
    }
}


function connectWebSocket() {

    if (pageIsClosing === true) {

        return;
    }


    if (socket !== null
            && (socket.readyState
            === WebSocket.OPEN

            || socket.readyState
            === WebSocket.CONNECTING)) {

        return;
    }


    try {

        socket =
            new WebSocket(
                "ws://localhost:9090"
            );


        socket.addEventListener(
            "open",
            function () {

                console.log(
                    "WebSocket connected"
                );


                if (reconnectTimer !== null) {

                    clearTimeout(
                        reconnectTimer
                    );

                    reconnectTimer =
                        null;
                }
            }
        );


        socket.addEventListener(
            "message",
            function (event) {

                receiveWebSocketMessage(
                    event.data
                );
            }
        );


        socket.addEventListener(
            "close",
            function () {

                console.log(
                    "WebSocket disconnected"
                );


                socket = null;


                clearSendTimer();


                sendButton.disabled =
                    false;

                sendButton.innerText =
                    "Send";


                scheduleWebSocketReconnect();
            }
        );


        socket.addEventListener(
            "error",
            function () {

                console.log(
                    "WebSocket connection error"
                );
            }
        );


    } catch (error) {

        socket = null;

        scheduleWebSocketReconnect();
    }
}


function scheduleWebSocketReconnect() {

    if (pageIsClosing === true
            || reconnectTimer !== null) {

        return;
    }


    reconnectTimer =
        setTimeout(
            function () {

                reconnectTimer =
                    null;

                connectWebSocket();
            },

            3000
        );
}


function receiveWebSocketMessage(
    messageData
) {

    let data;


    try {

        data =
            JSON.parse(
                messageData
            );


    } catch (error) {

        return;
    }


    if (data.success === false) {

        clearSendTimer();


        messageStatus.style.color =
            "red";

        messageStatus.innerText =
            data.message;


        sendButton.disabled =
            false;

        sendButton.innerText =
            "Send";

        return;
    }


    if (data.type !== "new_message") {

        return;
    }


    if (String(data.chatId)
            !== String(chatId)) {

        return;
    }


    clearSendTimer();


    if (String(data.senderId)
            === String(userId)) {

        message.value =
            "";

        messageStatus.innerText =
            "";
    }


    sendButton.disabled =
        false;

    sendButton.innerText =
        "Send";


    loadMessages();
}


function clearSendTimer() {

    if (sendTimer !== null) {

        clearTimeout(
            sendTimer
        );

        sendTimer =
            null;
    }
}


if (messageSearch !== null) {

    messageSearch.addEventListener(
        "input",
        searchMessages
    );
}


if (clearSearchBtn !== null) {

    clearSearchBtn.addEventListener(
        "click",
        function () {

            messageSearch.value = "";

            searchMessages();

            messageSearch.focus();
        }
    );
}


sendButton.addEventListener(
    "click",
    sendMessage
);


message.addEventListener(
    "keydown",
    function (event) {

        if (event.key === "Enter") {

            event.preventDefault();

            sendMessage();
        }
    }
);


if (chatInfoLink !== null) {

    chatInfoLink.addEventListener(
        "click",
        function () {

            window.location.href =
                "ChatInfo.html?chatId="
                + encodeURIComponent(chatId)

                + "&name="
                + encodeURIComponent(name)

                + "&type="
                + encodeURIComponent(type);
        }
    );
}


if (historyBtn !== null) {

    historyBtn.addEventListener(
        "click",
        function () {

            window.location.href =
                "History.html?chatId="
                + encodeURIComponent(chatId)

                + "&name="
                + encodeURIComponent(name)

                + "&type="
                + encodeURIComponent(type);
        }
    );
}


window.addEventListener(
    "beforeunload",
    function () {

        pageIsClosing =
            true;


        clearSendTimer();


        if (reconnectTimer !== null) {

            clearTimeout(
                reconnectTimer
            );

            reconnectTimer =
                null;
        }


        if (socket !== null) {

            socket.close();
        }
    }
);
function markCurrentChatAsRead() {

    if (userId === null
            || chatId === null) {

        return;
    }


    let key =
        "chat_last_read_"
        + userId
        + "_"
        + chatId;


    localStorage.setItem(
        key,
        String(Date.now())
    );
}
