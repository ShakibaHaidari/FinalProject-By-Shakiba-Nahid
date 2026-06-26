const API_URL = "http://localhost:8080";
const CHAT_ID = "g1";

const messagesBox = document.getElementById("messages");
const messageInput = document.getElementById("message");
const sendButton = document.getElementById("sendButton");
const chatStatus = document.getElementById("chatStatus");
const messageStatus = document.getElementById("messageStatus");

const senderId = localStorage.getItem("userId");
const username = localStorage.getItem("username");

if (!senderId) {
    chatStatus.textContent = "Please log in first.";
    messageStatus.textContent = "No logged-in user found.";
    messageStatus.style.color = "red";
} else {
    chatStatus.textContent = "Logged in as " + username;
    loadMessages();
}

async function loadMessages() {
    try {
        const response = await fetch(
            API_URL + "/api/messages?chatId=" + encodeURIComponent(CHAT_ID)
        );

        if (!response.ok) {
            throw new Error("Cannot load messages.");
        }

        const messages = await response.json();

        messagesBox.innerHTML = "";

        messages.forEach(function (message) {
            addMessageToScreen(
                message.content,
                message.senderId === senderId
            );
        });

        messagesBox.scrollTop = messagesBox.scrollHeight;
    } catch (error) {
        console.error(error);
        messageStatus.textContent = "Cannot load messages. Run Main.java first.";
        messageStatus.style.color = "red";
    }
}
async function sendMessage() {
    const text = messageInput.value.trim();
    if (text === "") {
        return;
    }
    if (!senderId) {
        messageStatus.textContent = "Please log in first.";
        messageStatus.style.color = "red";
        return;
    }
    try {
        const response = await fetch(API_URL + "/api/messages", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                chatId: CHAT_ID,
                senderId: senderId,
                content: text
            })
        });
        const responseText = await response.text();
        console.log("Message API response:", response.status, responseText);
        if (!response.ok) {
            throw new Error(responseText || "Message could not be sent.");
        }
        messageInput.value = "";
        messageStatus.textContent = "";
        await loadMessages();
    } catch (error) {
        console.error(error);
        messageStatus.textContent =
            "Cannot send message: " + error.message;
        messageStatus.style.color = "red";
    }
}
function addMessageToScreen(text, isMyMessage) {
    const div = document.createElement("div");

    div.className = isMyMessage
        ? "message my-message"
        : "message";
    div.textContent = text;
    messagesBox.appendChild(div);
}
sendButton.addEventListener("click", sendMessage);
messageInput.addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
        sendMessage();
    }
});