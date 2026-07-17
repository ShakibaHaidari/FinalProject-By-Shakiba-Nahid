let api =
    "http://localhost:8080";


let archiveList =
    document.getElementById(
        "archiveList"
    );

let search =
    document.getElementById(
        "search"
    );

let refreshBtn =
    document.getElementById(
        "refreshBtn"
    );

let pageMessage =
    document.getElementById(
        "pageMessage"
    );


let userId =
    localStorage.getItem(
        "userId"
    );


if (userId === null) {

    userId =
        sessionStorage.getItem(
            "userId"
        );
}


if (userId === null) {

    window.location.href =
        "Login.html";

} else {

    loadArchivedChats();
}


refreshBtn.addEventListener(
    "click",
    function () {

        loadArchivedChats();
    }
);


search.addEventListener(
    "input",
    function () {

        let text =
            search.value
                .trim()
                .toLowerCase();


        let chats =
            document.getElementsByClassName(
                "archive-chat"
            );


        for (let i = 0;
             i < chats.length;
             i++) {


            let name =
                chats[i]
                    .getAttribute(
                        "data-name"
                    );


            let id =
                chats[i]
                    .getAttribute(
                        "data-id"
                    );


            if (name.includes(text)
                    || id.includes(text)) {

                chats[i].style.display =
                    "flex";

            } else {

                chats[i].style.display =
                    "none";
            }
        }
    }
);


async function loadArchivedChats() {

    archiveList.innerHTML =
        '<p class="loading">'
        + 'Loading archived chats...'
        + '</p>';


    pageMessage.innerText = "";


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
                + encodeURIComponent(
                    userId
                )
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


        let chatMap = {};


        for (let i = 0;
             i < users.length;
             i++) {


            if (String(users[i].id)
                    !== String(userId)) {


                let personalId =
                    makePersonalChatId(
                        userId,
                        users[i].id
                    );


                chatMap[personalId] = {

                    id:
                        personalId,

                    name:
                        users[i].username,

                    type:
                        "user",

                    profileUserId:
                        users[i].id,

                    text:
                        "Personal chat"
                };
            }
        }


        for (let i = 0;
             i < groups.length;
             i++) {


            chatMap[
                groups[i].id
            ] = {

                id:
                    groups[i].id,

                name:
                    groups[i].name,

                type:
                    "group",

                text:
                    groups[i].memberCount
                    + " members"
            };
        }


        let archivedChats = [];


        for (let i = 0;
             i < settings.length;
             i++) {


            if (settings[i].archived
                    === true) {


                let chatData =
                    chatMap[
                        settings[i].chatId
                    ];


                if (chatData === undefined) {

                    chatData = {

                        id:
                            settings[i].chatId,

                        name:
                            settings[i].chatId,

                        type:
                            String(
                                settings[i].chatId
                            ).startsWith("p_")
                                ? "user"
                                : "group",

                        text:
                            "Archived chat"
                    };
                }


                chatData.updatedAt =
                    settings[i].updatedAt;


                chatData.pinned =
                    settings[i].pinned
                    === true;


                archivedChats.push(
                    chatData
                );
            }
        }


        archivedChats.sort(function (first, second) {

                return String(
                    second.updatedAt
                ).localeCompare(
                    String(
                        first.updatedAt
                    )
                );
            }
        );


        archiveList.innerHTML = "";


        if (archivedChats.length === 0) {

            archiveList.innerHTML =
                '<p class="empty">'
                + 'No archived chats'
                + '</p>';

            return;
        }


        for (let i = 0;
             i < archivedChats.length;
             i++) {

            addArchivedChat(
                archivedChats[i]
            );
        }


    } catch (error) {

        archiveList.innerHTML =
            '<p class="empty">'
            + 'Server is not connected'
            + '</p>';
    }
}


function addArchivedChat(chatData){

    let box =
        document.createElement(
            "div"
        );


    box.className =
        "archive-chat";


    box.setAttribute(
        "data-name",
        chatData.name.toLowerCase()
    );


    box.setAttribute(
        "data-id",
        chatData.id.toLowerCase()
    );


    let main =
        document.createElement(
            "div"
        );


    main.className =
        "archive-main";


    let profile =
        document.createElement(
            "div"
        );


    profile.className =
        "profile";


    profile.innerText =
        chatData.name
            .charAt(0)
            .toUpperCase();
            if (chatData.type === "user"
                    && chatData.profileUserId !== undefined) {

                loadArchiveUserPicture(
                    profile,
                    chatData.profileUserId
                );

            } else if (chatData.type === "group") {

                loadArchiveGroupPicture(
                    profile,
                    chatData.id
                );
            }


    let info =
        document.createElement(
            "div"
        );


    let title =
        document.createElement(
            "h3"
        );


    if (chatData.pinned === true) {

        title.innerText =
            "📌 " + chatData.name;

    } else {

        title.innerText =
            chatData.name;
    }


    let detail =
        document.createElement(
            "p"
        );


    detail.innerText =
        chatData.text;


    let date =
        document.createElement(
            "p"
        );


    date.className =
        "archive-date";


    date.innerText =
        "Archived: "
        + showDate(
            chatData.updatedAt
        );


    info.appendChild(title);

    info.appendChild(detail);

    info.appendChild(date);


    main.appendChild(profile);

    main.appendChild(info);


    main.addEventListener(
        "click",
        function () {

            openChat(chatData);
        }
    );


    let actions =
        document.createElement(
            "div"
        );


    actions.className =
        "archive-actions";


    let openBtn =
        document.createElement(
            "button"
        );


    openBtn.innerText =
        "Open";


    openBtn.addEventListener(
        "click",
        function () {

            openChat(chatData);
        }
    );


    let removeBtn =
        document.createElement(
            "button"
        );


    removeBtn.className =
        "remove-btn";


    removeBtn.innerText =
        "Unarchive";


    removeBtn.addEventListener(
        "click",
        function () {

            unarchiveChat(
                chatData.id
            );
        }
    );


    actions.appendChild(openBtn);

    actions.appendChild(removeBtn);


    box.appendChild(main);

    box.appendChild(actions);


    archiveList.appendChild(box);
}


function openChat(chatData) {

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


async function unarchiveChat(
    chatId
) {

    let body =
        "userId="
        + encodeURIComponent(userId)
        + "&chatId="
        + encodeURIComponent(chatId)
        + "&archived=false";


    try {

        let response =
            await fetch(
                api
                + "/api/chats/archive",

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
                "Chat removed from archive";


            loadArchivedChats();

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
async function loadArchiveUserPicture(
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
            "User picture could not be loaded"
        );
    }
}


async function loadArchiveGroupPicture(
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