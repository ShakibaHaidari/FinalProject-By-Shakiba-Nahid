let api =
    "http://localhost:8080";


let groupBtn =
    document.getElementById(
        "groupBtn"
    );

let groupBox =
    document.getElementById(
        "groupBox"
    );

let groupId =
    document.getElementById(
        "groupId"
    );

let groupName =
    document.getElementById(
        "groupName"
    );

let createBtn =
    document.getElementById(
        "createBtn"
    );

let groupMessage =
    document.getElementById(
        "groupMessage"
    );

let contactId =
    document.getElementById(
        "contactId"
    );

let addContactBtn =
    document.getElementById(
        "addContactBtn"
    );

let contactMessage =
    document.getElementById(
        "contactMessage"
    );

let search =
    document.getElementById(
        "search"
    );

let refreshContactsBtn =
    document.getElementById(
        "refreshContactsBtn"
    );

let contactList =
    document.getElementById(
        "contactList"
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

    loadContacts();
}


groupBtn.addEventListener(
    "click",
    function () {

        if (groupBox.style.display
                === "block") {

            groupBox.style.display =
                "none";

            groupBtn.innerText =
                "+ Create New Group";

        } else {

            groupBox.style.display =
                "block";

            groupBtn.innerText =
                "Close Group Form";
        }
    }
);


createBtn.addEventListener(
    "click",
    createGroup
);


addContactBtn.addEventListener(
    "click",
    addNewContact
);


contactId.addEventListener(
    "keydown",
    function (event) {

        if (event.key === "Enter") {

            addNewContact();
        }
    }
);


search.addEventListener(
    "input",
    searchContacts
);


refreshContactsBtn.addEventListener(
    "click",
    loadContacts
);


async function createGroup() {

    let id =
        groupId.value.trim();

    let name =
        groupName.value.trim();


    showGroupMessage(
        "",
        false
    );


    if (id === ""
            || name === "") {

        showGroupMessage(
            "Please fill in all fields",
            false
        );

        return;
    }


    if (id.length > 40
            || name.length > 60) {

        showGroupMessage(
            "Group ID or name is too long",
            false
        );

        return;
    }


    createBtn.disabled =
        true;

    createBtn.innerText =
        "Creating...";


    let body =
        "id="
        + encodeURIComponent(id)

        + "&name="
        + encodeURIComponent(name)

        + "&userId="
        + encodeURIComponent(userId);


    try {

        let response =
            await fetch(
                api + "/api/groups",

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


        showGroupMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            groupId.value = "";

            groupName.value = "";


            setTimeout(
                function () {

                    window.location.href =
                        "Home.html";
                },

                700
            );
        }


    } catch (error) {

        showGroupMessage(
            "Server is not connected",
            false
        );

    } finally {

        createBtn.disabled =
            false;

        createBtn.innerText =
            "Create Group";
    }
}


async function addNewContact() {

    let targetUserId =
        contactId.value.trim();


    if (targetUserId === "") {

        showContactMessage(
            "Please enter a User ID",
            false
        );

        return;
    }


    if (targetUserId
            .toLowerCase()
            === String(userId)
            .toLowerCase()) {

        showContactMessage(
            "You cannot add yourself",
            false
        );

        return;
    }


    addContactBtn.disabled =
        true;

    addContactBtn.innerText =
        "Adding...";


    let body =
        "action=addContact"

        + "&userId="
        + encodeURIComponent(userId)

        + "&targetUserId="
        + encodeURIComponent(
            targetUserId
        );


    try {

        let response =
            await fetch(
                api + "/api/chats/info",

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


        showContactMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            contactId.value = "";

            await loadContacts();
        }


    } catch (error) {

        showContactMessage(
            "Server is not connected",
            false
        );

    } finally {

        addContactBtn.disabled =
            false;

        addContactBtn.innerText =
            "Add Contact";
    }
}


async function loadContacts() {

    contactList.innerHTML =
        '<p class="loading">'
        + 'Loading contacts...'
        + '</p>';


    try {

        let response =
            await fetch(
                api
                + "/api/contacts?userId="
                + encodeURIComponent(userId)
            );


        let contacts =
            await response.json();


        if (Array.isArray(contacts)
                === false) {

            contactList.innerHTML =
                '<p class="empty">'
                + 'Contacts could not be loaded'
                + '</p>';

            return;
        }


        contactList.innerHTML = "";


        if (contacts.length === 0) {

            contactList.innerHTML =
                '<p class="empty">'
                + 'No contacts yet'
                + '</p>';

            return;
        }


        contacts.sort(
            function (
                first,
                second
            ) {

                return first.username
                    .localeCompare(
                        second.username
                    );
            }
        );


        for (let i = 0;
             i < contacts.length;
             i++) {

            addContactCard(
                contacts[i]
            );
        }


        searchContacts();


    } catch (error) {

        contactList.innerHTML =
            '<p class="empty">'
            + 'Server is not connected'
            + '</p>';
    }
}


function addContactCard(
    contactData
) {

    let contact =
        document.createElement(
            "div"
        );

    contact.className =
        "contact";

    contact.setAttribute(
        "data-name",
        contactData.username
            .toLowerCase()
    );

    contact.setAttribute(
        "data-id",
        String(contactData.id)
            .toLowerCase()
    );


    let main =
        document.createElement(
            "div"
        );

    main.className =
        "contact-main";


    let profile =
        document.createElement(
            "div"
        );

    profile.className =
        "profile";

    profile.innerText =
        contactData.username
            .charAt(0)
            .toUpperCase();


    loadContactPicture(
        profile,
        contactData.id
    );


    let information =
        document.createElement(
            "div"
        );

    information.className =
        "contact-information";


    let title =
        document.createElement(
            "h3"
        );

    title.innerText =
        contactData.username;


    let detail =
        document.createElement(
            "p"
        );

    detail.innerText =
        "User ID: "
        + contactData.id;


    information.appendChild(
        title
    );

    information.appendChild(
        detail
    );


    main.appendChild(
        profile
    );

    main.appendChild(
        information
    );


    main.addEventListener(
        "click",
        function () {

            openPersonalChat(
                contactData
            );
        }
    );


    let actions =
        document.createElement(
            "div"
        );

    actions.className =
        "contact-actions";


    let openButton =
        document.createElement(
            "button"
        );

    openButton.className =
        "open-button";

    openButton.innerText =
        "Open";


    openButton.addEventListener(
        "click",
        function () {

            openPersonalChat(
                contactData
            );
        }
    );


    let removeButton =
        document.createElement(
            "button"
        );

    removeButton.className =
        "remove-button";

    removeButton.innerText =
        "Remove";


    removeButton.addEventListener(
        "click",
        function () {

            removeContact(
                contactData
            );
        }
    );


    actions.appendChild(
        openButton
    );

    actions.appendChild(
        removeButton
    );


    contact.appendChild(
        main
    );

    contact.appendChild(
        actions
    );


    contactList.appendChild(
        contact
    );
}


async function removeContact(
    contactData
) {

    let answer =
        confirm(
            "Remove "
            + contactData.username
            + " from contacts?"
        );


    if (answer === false) {

        return;
    }


    let body =
        "action=removeContact"

        + "&userId="
        + encodeURIComponent(userId)

        + "&targetUserId="
        + encodeURIComponent(
            contactData.id
        );


    try {

        let response =
            await fetch(
                api + "/api/chats/info",

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


        showContactMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            loadContacts();
        }


    } catch (error) {

        showContactMessage(
            "Server is not connected",
            false
        );
    }
}


function openPersonalChat(
    contactData
) {

    let personalChatId =
        makePersonalChatId(
            userId,
            contactData.id
        );


    window.location.href =
        "Chat.html?chatId="
        + encodeURIComponent(
            personalChatId
        )

        + "&name="
        + encodeURIComponent(
            contactData.username
        )

        + "&type=user";
}


function makePersonalChatId(
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


function searchContacts() {

    let text =
        search.value
            .trim()
            .toLowerCase();


    let contacts =
        document.getElementsByClassName(
            "contact"
        );


    for (let i = 0;
         i < contacts.length;
         i++) {

        let name =
            contacts[i].getAttribute(
                "data-name"
            );

        let id =
            contacts[i].getAttribute(
                "data-id"
            );


        if (name.includes(text)
                || id.includes(text)) {

            contacts[i].style.display =
                "flex";

        } else {

            contacts[i].style.display =
                "none";
        }
    }
}


async function loadContactPicture(
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
            "Contact picture could not be loaded"
        );
    }
}


function showGroupMessage(
    text,
    success
) {

    if (text === "") {

        groupMessage.innerText = "";

        return;
    }


    if (success === true) {

        groupMessage.style.color =
            "green";

    } else {

        groupMessage.style.color =
            "red";
    }


    groupMessage.innerText =
        text;
}


function showContactMessage(
    text,
    success
) {

    if (success === true) {

        contactMessage.style.color =
            "green";

    } else {

        contactMessage.style.color =
            "red";
    }


    contactMessage.innerText =
        text;
}