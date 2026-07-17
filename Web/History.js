let historyList =
    document.getElementById("historyList");

let chatName =
    document.getElementById("chatName");

let backBtn =
    document.getElementById("backBtn");

let message =
    document.getElementById("message");


let address =
    new URLSearchParams(window.location.search);

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
        sessionStorage.getItem("userId");
}


if (userId === null) {

    window.location.href =
        "Login.html";
}


if (chatId === null) {

    window.location.href =
        "Home.html";
}


if (name !== null) {

    chatName.innerText =
        name + " History";
}


backBtn.addEventListener(
    "click",
    function () {

        window.location.href =
            "Chat.html?chatId="
            + encodeURIComponent(chatId)
            + "&name="
            + encodeURIComponent(name)
            + "&type="
            + encodeURIComponent(type);
    }
);


loadHistory();


async function loadHistory() {

    try {

        let response = await fetch(
            "http://localhost:8080/api/messages/history?chatId="
            + encodeURIComponent(chatId)
        );


        let data =
            await response.json();


        if (response.ok === false) {

            historyList.innerHTML = "";

            message.innerText =
                data.message;

            return;
        }


        historyList.innerHTML = "";


        if (data.length === 0) {

            historyList.innerHTML =
                '<p class="empty">'
                + 'No edited or deleted messages'
                + '</p>';

            return;
        }


        for (let i = 0; i < data.length; i++) {

            addHistory(data[i]);
        }


    } catch (error) {

        historyList.innerHTML = "";

        message.innerText =
            "Server is not connected";
    }
}


function addHistory(data) {

    let box =
        document.createElement("div");

    box.className =
        "history-item";


    let title =
        document.createElement("h3");

    title.innerText =
        "Sender: " + data.senderId;


    box.appendChild(title);


    if (data.edited === true) {

        let oldText =
            document.createElement("p");

        oldText.className =
            "old-text";

        oldText.innerText =
            "Before edit: "
            + data.previousContent;


        let newText =
            document.createElement("p");

        newText.className =
            "new-text";

        newText.innerText =
            "After edit: "
            + data.content;


        let editDate =
            document.createElement("p");

        editDate.className =
            "date";

        editDate.innerText =
            "Edited at: "
            + showDate(data.editedAt);


        box.appendChild(oldText);

        box.appendChild(newText);

        box.appendChild(editDate);
    }


    if (data.deleted === true) {

        let deletedText =
            document.createElement("p");

        deletedText.className =
            "deleted-text";

        deletedText.innerText =
            "Deleted message: "
            + data.content;


        let deleteDate =
            document.createElement("p");

        deleteDate.className =
            "date";

        deleteDate.innerText =
            "Deleted at: "
            + showDate(data.deletedAt);


        box.appendChild(deletedText);

        box.appendChild(deleteDate);
    }


    historyList.appendChild(box);
}


function showDate(date) {

    if (date === null
            || date === undefined
            || date === "") {

        return "Unknown";
    }


    return date
        .replace("T", " ")
        .substring(0, 16);
}