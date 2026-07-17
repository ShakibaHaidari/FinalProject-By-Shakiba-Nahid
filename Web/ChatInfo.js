let api = "http://localhost:8080";


let backBtn =
    document.getElementById("backBtn");

let infoPicture =
    document.getElementById("infoPicture");

let infoName =
    document.getElementById("infoName");

let infoId =
    document.getElementById("infoId");

let infoSubtitle =
    document.getElementById("infoSubtitle");

let pageMessage =
    document.getElementById("pageMessage");

let loading =
    document.getElementById("loading");


let userSection =
    document.getElementById("userSection");

let commonGroups =
    document.getElementById("commonGroups");

let blockBtn =
    document.getElementById("blockBtn");

let contactBtn =
    document.getElementById("contactBtn");

let archiveUserBtn =
    document.getElementById("archiveUserBtn");


let groupSection =
    document.getElementById("groupSection");

let membersList =
    document.getElementById("membersList");

let memberIdInput =
    document.getElementById("memberIdInput");

let addMemberBtn =
    document.getElementById("addMemberBtn");

let newGroupName =
    document.getElementById("newGroupName");

let changeGroupNameBtn =
    document.getElementById(
        "changeGroupNameBtn"
    );

let archiveGroupBtn =
    document.getElementById(
        "archiveGroupBtn"
    );

let groupHistoryBtn =
    document.getElementById(
        "groupHistoryBtn"
    );
    let groupPictureInput =
        document.getElementById(
            "groupPictureInput"
        );

    let saveGroupPictureBtn =
        document.getElementById(
            "saveGroupPictureBtn"
        );

    let removeGroupPictureBtn =
        document.getElementById(
            "removeGroupPictureBtn"
        );

    let selectedGroupImageData = "";

    let currentGroupImageData = "";

let leaveGroupBtn =
    document.getElementById(
        "leaveGroupBtn"
    );


let address =
    new URLSearchParams(
        window.location.search
    );

let chatId =
    address.get("chatId");

let name =
    address.get("name");

let type =
    address.get("type");


let userId =
    localStorage.getItem("userId");


if (userId === null) {

    userId =
        sessionStorage.getItem(
            "userId"
        );
}


let targetUserId =
    null;

let blocked =
    false;

let contact =
    false;


if (userId === null) {

    window.location.href =
        "Login.html";

} else if (chatId === null
        || type === null) {

    showMessage(
        "Chat information is incomplete",
        false
    );

} else {

    updateBackLink();

    loadChatInformation();
}


blockBtn.addEventListener(
    "click",
    changeBlock
);


contactBtn.addEventListener(
    "click",
    changeContact
);


archiveUserBtn.addEventListener(
    "click",
    archiveChat
);


addMemberBtn.addEventListener(
    "click",
    addMember
);


changeGroupNameBtn.addEventListener(
    "click",
    changeGroupName
);


archiveGroupBtn.addEventListener(
    "click",
    archiveChat
);


groupHistoryBtn.addEventListener(
    "click",
    openHistory
);


leaveGroupBtn.addEventListener(
    "click",
    leaveGroup
);
groupPictureInput.addEventListener(
    "change",
    previewGroupPicture
);

saveGroupPictureBtn.addEventListener(
    "click",
    saveGroupPicture
);

removeGroupPictureBtn.addEventListener(
    "click",
    removeGroupPicture
);


async function loadChatInformation() {

    loading.style.display =
        "block";


    userSection.style.display =
        "none";


    groupSection.style.display =
        "none";


    showMessage(
        "",
        false
    );


    if (type === "user") {

        await loadUserInformation();

    } else if (type === "group") {

        await loadGroupInformation();

    } else {

        showMessage(
            "Unknown chat type",
            false
        );
    }


    loading.style.display =
        "none";
}


async function loadUserInformation() {

    try {

        targetUserId =
            await findTargetUserId();


        if (targetUserId === null) {

            showMessage(
                "User could not be found",
                false
            );

            return;
        }


        let url =
            api
            + "/api/chats/info?type=user"
            + "&userId="
            + encodeURIComponent(userId)
            + "&chatId="
            + encodeURIComponent(chatId)
            + "&targetUserId="
            + encodeURIComponent(
                targetUserId
            );


        let response =
            await fetch(url);


        let data =
            await response.json();


        if (data.success !== true) {

            showMessage(
                data.message,
                false
            );

            return;
        }


        name =
            data.username;


        blocked =
            data.blocked === true;


        contact =
            data.contact === true;


        infoName.innerText =
            data.username;


        infoId.innerText =
            "User ID: " + data.id;


        infoSubtitle.innerText =
            "Personal chat";


        showPictureLetter(
            data.username
        );


        loadUserPicture(
            data.id
        );


        showCommonGroups(
            data.commonGroups
        );


        updateUserButtons();


        userSection.style.display =
            "block";


        updateBackLink();


    } catch (error) {

        showMessage(
            "Server is not connected",
            false
        );
    }
}


async function loadGroupInformation() {

    try {

        let url =
            api
            + "/api/chats/info?type=group"
            + "&userId="
            + encodeURIComponent(userId)
            + "&chatId="
            + encodeURIComponent(chatId);


        let response =
            await fetch(url);


        let data =
            await response.json();


        if (data.success !== true) {

            showMessage(
                data.message,
                false
            );

            return;
        }


        name =
            data.name;


        infoName.innerText =
            data.name;


        infoId.innerText =
            "Group ID: " + data.id;


        infoSubtitle.innerText =
            data.memberCount
            + " members";


        newGroupName.value =
            data.name;


        showPictureLetter(
            data.name
        );
        await loadGroupPicture();


        showMembers(
            data.members
        );


        groupSection.style.display =
            "block";


        updateBackLink();


    } catch (error) {

        showMessage(
            "Server is not connected",
            false
        );
    }
}


async function findTargetUserId() {

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


        let personalId =
            makePersonalChatId(
                userId,
                users[i].id
            );


        if (String(personalId)
                === String(chatId)) {

            return users[i].id;
        }
    }


    return null;
}


async function loadUserPicture(
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

            infoPicture.innerText =
                "";


            infoPicture.style.backgroundImage =
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


function showCommonGroups(groups) {

    commonGroups.innerHTML = "";


    if (Array.isArray(groups)
            === false
            || groups.length === 0) {

        commonGroups.innerHTML =
            '<p class="empty-text">'
            + 'No common groups'
            + '</p>';

        return;
    }


    for (let i = 0;
         i < groups.length;
         i++) {

        let item =
            document.createElement(
                "div"
            );


        item.className =
            "small-item";


        item.innerText =
            groups[i].name
            + " ("
            + groups[i].id
            + ")";


        commonGroups.appendChild(
            item
        );
    }
}


function showMembers(groupMembers) {

    membersList.innerHTML = "";


    if (Array.isArray(groupMembers)
            === false
            || groupMembers.length === 0) {

        membersList.innerHTML =
            '<p class="empty-text">'
            + 'No group members'
            + '</p>';

        return;
    }


    for (let i = 0;
         i < groupMembers.length;
         i++) {

        let row =
            document.createElement(
                "div"
            );


        row.className =
            "member-row";


        let information =
            document.createElement(
                "div"
            );


        let memberName =
            document.createElement(
                "h4"
            );


        memberName.innerText =
            groupMembers[i].username;


        let memberId =
            document.createElement(
                "p"
            );


        memberId.innerText =
            "User ID: "
            + groupMembers[i].id;


        information.appendChild(
            memberName
        );


        information.appendChild(
            memberId
        );


        row.appendChild(
            information
        );


        if (String(groupMembers[i].id)
                !== String(userId)) {

            let removeBtn =
                document.createElement(
                    "button"
                );


            removeBtn.className =
                "small-danger-button";


            removeBtn.innerText =
                "Remove";


            removeBtn.addEventListener(
                "click",
                function () {

                    removeMember(
                        groupMembers[i].id
                    );
                }
            );


            row.appendChild(
                removeBtn
            );
        }


        membersList.appendChild(
            row
        );
    }
}


function updateUserButtons() {

    if (blocked === true) {

        blockBtn.innerText =
            "Unblock User";

    } else {

        blockBtn.innerText =
            "Block User";
    }


    if (contact === true) {

        contactBtn.innerText =
            "Remove from Contacts";

    } else {

        contactBtn.innerText =
            "Add to Contacts";
    }
}


async function changeBlock() {

    let action;


    if (blocked === true) {

        action =
            "unblock";

    } else {

        action =
            "block";
    }


    let data =
        await sendChatInfoRequest(

            "action="
            + encodeURIComponent(action)

            + "&userId="
            + encodeURIComponent(userId)

            + "&targetUserId="
            + encodeURIComponent(
                targetUserId
            )
        );


    if (data !== null) {

        showMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            blocked =
                !blocked;


            updateUserButtons();
        }
    }
}


async function changeContact() {

    let action;


    if (contact === true) {

        action =
            "removeContact";

    } else {

        action =
            "addContact";
    }


    let data =
        await sendChatInfoRequest(

            "action="
            + encodeURIComponent(action)

            + "&userId="
            + encodeURIComponent(userId)

            + "&targetUserId="
            + encodeURIComponent(
                targetUserId
            )
        );


    if (data !== null) {

        showMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            contact =
                !contact;


            updateUserButtons();
        }
    }
}


async function addMember() {

    let memberId =
        memberIdInput.value.trim();


    if (memberId === "") {

        showMessage(
            "Enter the user ID",
            false
        );

        return;
    }


    let data =
        await sendChatInfoRequest(

            "action=addMember"

            + "&userId="
            + encodeURIComponent(userId)

            + "&groupId="
            + encodeURIComponent(chatId)

            + "&memberId="
            + encodeURIComponent(memberId)
        );


    if (data !== null) {

        showMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            memberIdInput.value =
                "";


            loadGroupInformation();
        }
    }
}


async function removeMember(
    memberId
) {

    let answer =
        confirm(
            "Remove this member from the group?"
        );


    if (answer === false) {

        return;
    }


    let data =
        await sendChatInfoRequest(

            "action=removeMember"

            + "&userId="
            + encodeURIComponent(userId)

            + "&groupId="
            + encodeURIComponent(chatId)

            + "&memberId="
            + encodeURIComponent(memberId)
        );


    if (data !== null) {

        showMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            loadGroupInformation();
        }
    }
}


async function changeGroupName() {

    let newName =
        newGroupName.value.trim();


    if (newName === "") {

        showMessage(
            "Group name cannot be empty",
            false
        );

        return;
    }


    let data =
        await sendChatInfoRequest(

            "action=changeGroupName"

            + "&userId="
            + encodeURIComponent(userId)

            + "&groupId="
            + encodeURIComponent(chatId)

            + "&newName="
            + encodeURIComponent(newName)
        );


    if (data !== null) {

        showMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            name =
                newName;


            loadGroupInformation();
        }
    }
}


async function leaveGroup() {

    let answer =
        confirm(
            "Do you want to leave this group?"
        );


    if (answer === false) {

        return;
    }


    let data =
        await sendChatInfoRequest(

            "action=leaveGroup"

            + "&userId="
            + encodeURIComponent(userId)

            + "&groupId="
            + encodeURIComponent(chatId)
        );


    if (data !== null) {

        showMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            setTimeout(
                function () {

                    window.location.href =
                        "Home.html";
                },

                700
            );
        }
    }
}


async function archiveChat() {

    let answer =
        confirm(
            "Archive this chat?"
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


        showMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            setTimeout(
                function () {

                    window.location.href =
                        "Home.html";
                },

                700
            );
        }


    } catch (error) {

        showMessage(
            "Server is not connected",
            false
        );
    }
}


async function sendChatInfoRequest(
    body
) {

    try {

        let response =
            await fetch(

                api
                + "/api/chats/info",

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

        showMessage(
            "Server is not connected",
            false
        );


        return null;
    }
}


function openHistory() {

    window.location.href =
        "History.html?chatId="
        + encodeURIComponent(chatId)

        + "&name="
        + encodeURIComponent(name)

        + "&type="
        + encodeURIComponent(type);
}


function updateBackLink() {

    let finalName =
        name;


    if (finalName === null) {

        finalName =
            "Chat";
    }


    backBtn.href =
        "Chat.html?chatId="
        + encodeURIComponent(chatId)

        + "&name="
        + encodeURIComponent(finalName)

        + "&type="
        + encodeURIComponent(type);
}


function showPictureLetter(title) {

    infoPicture.style.backgroundImage =
        "none";


    if (title === null
            || title === undefined
            || title === "") {

        infoPicture.innerText =
            "?";

    } else {

        infoPicture.innerText =
            title
                .charAt(0)
                .toUpperCase();
    }
}


function showMessage(
    text,
    success
) {

    pageMessage.innerText =
        text;


    if (success === true) {

        pageMessage.style.color =
            "green";

    } else {

        pageMessage.style.color =
            "red";
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
async function loadGroupPicture() {

    try {

        let response =
            await fetch(
                api
                + "/api/groups/profile-picture?groupId="
                + encodeURIComponent(chatId)
            );

        let data =
            await response.json();

        if (data.success !== true) {

            showMessage(
                data.message,
                false
            );

            return;
        }

        currentGroupImageData =
            data.imageData;

        selectedGroupImageData = "";

        if (currentGroupImageData !== "") {

            showGroupPicture(
                currentGroupImageData
            );

        } else {

            showPictureLetter(name);
        }

    } catch (error) {

        showMessage(
            "Group picture could not be loaded",
            false
        );
    }
}


function previewGroupPicture() {

    let file =
        groupPictureInput.files[0];

    if (file === undefined) {
        return;
    }

    if (file.type !== "image/png"
            && file.type !== "image/jpeg"
            && file.type !== "image/webp") {

        showMessage(
            "Only PNG, JPG and WebP images are allowed",
            false
        );

        groupPictureInput.value = "";

        selectedGroupImageData = "";

        restoreGroupPicture();

        return;
    }

    if (file.size > 1000000) {

        showMessage(
            "Group picture must be smaller than 1 MB",
            false
        );

        groupPictureInput.value = "";

        selectedGroupImageData = "";

        restoreGroupPicture();

        return;
    }

    let reader =
        new FileReader();

    reader.onload =
        function () {

            selectedGroupImageData =
                reader.result;

            showGroupPicture(
                selectedGroupImageData
            );

            showMessage(
                "Picture selected. Click Save Picture.",
                true
            );
        };

    reader.readAsDataURL(file);
}


async function saveGroupPicture() {

    if (selectedGroupImageData === "") {

        showMessage(
            "Please select a group picture first",
            false
        );

        return;
    }

    saveGroupPictureBtn.disabled =
        true;

    saveGroupPictureBtn.innerText =
        "Saving...";

    let body =
        "groupId="
        + encodeURIComponent(chatId)

        + "&action=save"

        + "&imageData="
        + encodeURIComponent(
            selectedGroupImageData
        );

    let data =
        await sendGroupPictureRequest(
            body
        );

    if (data !== null) {

        showMessage(
            data.message,
            data.success
        );

        if (data.success === true) {

            currentGroupImageData =
                selectedGroupImageData;

            selectedGroupImageData = "";

            groupPictureInput.value = "";

            showGroupPicture(
                currentGroupImageData
            );
        }
    }

    saveGroupPictureBtn.disabled =
        false;

    saveGroupPictureBtn.innerText =
        "Save Picture";
}


async function removeGroupPicture() {

    if (currentGroupImageData === "") {

        selectedGroupImageData = "";

        groupPictureInput.value = "";

        showPictureLetter(name);

        showMessage(
            "There is no saved group picture",
            false
        );

        return;
    }

    let answer =
        confirm(
            "Do you want to remove the group picture?"
        );

    if (answer === false) {
        return;
    }

    removeGroupPictureBtn.disabled =
        true;

    removeGroupPictureBtn.innerText =
        "Removing...";

    let body =
        "groupId="
        + encodeURIComponent(chatId)

        + "&action=remove";

    let data =
        await sendGroupPictureRequest(
            body
        );

    if (data !== null) {

        showMessage(
            data.message,
            data.success
        );

        if (data.success === true) {

            currentGroupImageData = "";

            selectedGroupImageData = "";

            groupPictureInput.value = "";

            showPictureLetter(name);
        }
    }

    removeGroupPictureBtn.disabled =
        false;

    removeGroupPictureBtn.innerText =
        "Remove Picture";
}


async function sendGroupPictureRequest(
    body
) {

    try {

        let response =
            await fetch(
                api
                + "/api/groups/profile-picture",

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

        showMessage(
            "Server is not connected",
            false
        );

        return null;
    }
}


function showGroupPicture(
    imageData
) {

    infoPicture.innerText = "";

    infoPicture.style.backgroundImage =
        "url('" + imageData + "')";
}


function restoreGroupPicture() {

    if (currentGroupImageData !== "") {

        showGroupPicture(
            currentGroupImageData
        );

    } else {

        showPictureLetter(name);
    }
}