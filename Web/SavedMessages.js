let api =
    "http://localhost:8080";


let savedList =
    document.getElementById("savedList");

let search =
    document.getElementById("search");

let refreshBtn =
    document.getElementById("refreshBtn");

let pageMessage =
    document.getElementById("message");


let userId =
    localStorage.getItem("userId");


if (userId === null) {

    userId =
        sessionStorage.getItem("userId");
}


if (userId === null) {

    window.location.href =
        "Login.html";

} else {

    loadSavedMessages();
}


refreshBtn.addEventListener(
    "click",
    function () {

        loadSavedMessages();
    }
);


search.addEventListener(
    "input",
    function () {

        let searchText =
            search.value
                .trim()
                .toLowerCase();


        let savedMessages =
            document.getElementsByClassName(
                "saved-message"
            );


        for (let i = 0;
             i < savedMessages.length;
             i++) {

            let content =
                savedMessages[i]
                    .getAttribute(
                        "data-content"
                    );

            let sender =
                savedMessages[i]
                    .getAttribute(
                        "data-sender"
                    );

            let chatId =
                savedMessages[i]
                    .getAttribute(
                        "data-chat"
                    );


            if (content.includes(searchText)
                    || sender.includes(searchText)
                    || chatId.includes(searchText)) {

                savedMessages[i]
                    .style.display =
                    "block";

            } else {

                savedMessages[i]
                    .style.display =
                    "none";
            }
        }
    }
);


async function loadSavedMessages() {

    savedList.innerHTML =
        '<p class="loading">'
        + 'Loading saved messages...'
        + '</p>';


    pageMessage.innerText = "";


    try {

        let response =
            await fetch(
                api
                + "/api/saved-messages?userId="
                + encodeURIComponent(userId)
            );


        let data =
            await response.json();


        if (response.ok === false) {

            savedList.innerHTML = "";

            if (data.message !== undefined) {

                pageMessage.innerText =
                    data.message;

            } else {

                pageMessage.innerText =
                    "Cannot load saved messages";
            }

            return;
        }


        if (Array.isArray(data) === false) {

            throw new Error(
                "Invalid server response"
            );
        }


        savedList.innerHTML = "";


        if (data.length === 0) {

            savedList.innerHTML =
                '<p class="empty">'
                + 'No saved messages yet'
                + '</p>';

            return;
        }


        for (let i = 0;
             i < data.length;
             i++) {

            addSavedMessage(data[i]);
        }


    } catch (error) {

        savedList.innerHTML = "";

        pageMessage.style.color =
            "red";

        pageMessage.innerText =
            "Server is not connected";
    }
}


function addSavedMessage(data) {

    let content =
        data.content;


    if (content === undefined
            || content === null
            || content === "") {

        content =
            "Message content is unavailable";
    }


    let senderId =
        data.senderId;


    if (senderId === undefined
            || senderId === null) {

        senderId =
            "Unknown";
    }


    let chatId =
        data.chatId;


    if (chatId === undefined
            || chatId === null) {

        chatId =
            "";
    }


    let box =
        document.createElement("div");


    box.className =
        "saved-message";


    box.setAttribute(
        "data-content",
        String(content).toLowerCase()
    );


    box.setAttribute(
        "data-sender",
        String(senderId).toLowerCase()
    );


    box.setAttribute(
        "data-chat",
        String(chatId).toLowerCase()
    );


    let text =
        document.createElement("p");


    text.className =
        "message-content";


    text.innerText =
        content;


    let details =
        document.createElement("div");


    details.className =
        "message-details";


    let sender =
        document.createElement("p");


    if (String(senderId)
            === String(userId)) {

        sender.innerText =
            "Sender: You";

    } else {

        sender.innerText =
            "Sender: " + senderId;
    }


    let chat =
        document.createElement("p");


    chat.innerText =
        "Chat ID: " + chatId;


    let date =
        document.createElement("p");


    date.innerText =
        "Date: "
        + showDate(data.createdAt);


    details.appendChild(sender);

    details.appendChild(chat);

    details.appendChild(date);


    if (data.edited === true
            || data.deleted === true) {

        let status =
            document.createElement("p");


        status.className =
            "message-status";


        if (data.deleted === true) {

            status.innerText =
                "Status: Deleted message";

        } else {

            status.innerText =
                "Status: Edited message";
        }


        details.appendChild(status);
    }


    let actions =
        document.createElement("div");


    actions.className =
        "saved-actions";


    if (chatId !== "") {

        let openBtn =
            document.createElement("button");


        openBtn.className =
            "open-chat";


        openBtn.innerText =
            "Open Chat";


        openBtn.addEventListener(
            "click",
            function () {

                openChat(chatId);
            }
        );


        actions.appendChild(openBtn);
    }


    let removeBtn =
        document.createElement("button");


    removeBtn.className =
        "remove-saved";


    removeBtn.innerText =
        "Remove";


    removeBtn.addEventListener(
        "click",
        function () {

            removeSavedMessage(
                data.id
            );
        }
    );


    actions.appendChild(removeBtn);


    box.appendChild(text);

    box.appendChild(details);

    box.appendChild(actions);


    savedList.appendChild(box);
}


async function removeSavedMessage(
    messageId
) {

    let answer =
        confirm(
            "Remove this message from Saved Messages?"
        );


    if (answer === false) {

        return;
    }


    let body =
        "userId="
        + encodeURIComponent(userId)
        + "&messageId="
        + encodeURIComponent(messageId);


    try {

        let response =
            await fetch(
                api
                + "/api/saved-messages/remove",

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

            pageMessage.style.color =
                "green";


            pageMessage.innerText =
                "Saved message removed successfully";


            loadSavedMessages();

        } else {

            pageMessage.style.color =
                "red";


            pageMessage.innerText =
                data.message;
        }


    } catch (error) {

        pageMessage.style.color =
            "red";


        pageMessage.innerText =
            "Server is not connected";
    }
}


function openChat(chatId) {

    let chatType =
        "group";


    if (String(chatId)
            .startsWith("p_")) {

        chatType =
            "user";
    }


    window.location.href =
        "Chat.html?chatId="
        + encodeURIComponent(chatId)
        + "&name="
        + encodeURIComponent(
            "Saved Message"
        )
        + "&type="
        + encodeURIComponent(chatType);
}


function showDate(date) {

    if (date === undefined
            || date === null
            || date === "") {

        return "Unknown";
    }


    return String(date)
        .replace("T", " ")
        .substring(0, 16);
}