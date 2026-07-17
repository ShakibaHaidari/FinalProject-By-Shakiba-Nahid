let mediaInput =
    document.getElementById(
        "mediaInput"
    );

let selectedMediaName =
    document.getElementById(
        "selectedMediaName"
    );

let selectedMediaPreview =
    document.getElementById(
        "selectedMediaPreview"
    );

let sendMediaButton =
    document.getElementById(
        "sendMediaButton"
    );

let clearMediaButton =
    document.getElementById(
        "clearMediaButton"
    );

let selectedMediaFile =
    null;

let mediaPreviewUrl =
    null;

let lastMessageSnapshot =
    null;

let mediaObserverRunning =
    false;


mediaInput.addEventListener(
    "change",
    selectMediaFile
);


sendMediaButton.addEventListener(
    "click",
    sendSelectedMedia
);


clearMediaButton.addEventListener(
    "click",
    clearSelectedMedia
);


function selectMediaFile() {

    let file =
        mediaInput.files[0];


    if (file === undefined) {

        clearSelectedMedia();

        return;
    }


    if (file.size === 0) {

        showMediaStatus(
            "The selected file is empty",
            false
        );

        clearSelectedMedia();

        return;
    }


    if (file.size > 5000000) {

        showMediaStatus(
            "File must be smaller than 5 MB",
            false
        );

        clearSelectedMedia();

        return;
    }


    selectedMediaFile =
        file;


    selectedMediaName.innerText =
        file.name;


    showSelectedMediaPreview(
        file
    );


    showMediaStatus(
        "File selected. Click Send File.",
        true
    );
}


function showSelectedMediaPreview(
    file
) {

    clearPreviewUrl();


    selectedMediaPreview.innerHTML =
        "";


    if (file.type.startsWith(
            "image/"
    )) {

        mediaPreviewUrl =
            URL.createObjectURL(
                file
            );


        let image =
            document.createElement(
                "img"
            );


        image.src =
            mediaPreviewUrl;


        image.alt =
            file.name;


        selectedMediaPreview.appendChild(
            image
        );

        selectedMediaPreview.style.display =
            "block";

        return;
    }


    let fileText =
        document.createElement(
            "p"
        );


    fileText.innerText =
        "Selected file: "
        + file.name;


    selectedMediaPreview.appendChild(
        fileText
    );


    selectedMediaPreview.style.display =
        "block";
}


function clearSelectedMedia() {

    selectedMediaFile =
        null;


    mediaInput.value =
        "";


    selectedMediaName.innerText =
        "No file selected";


    selectedMediaPreview.innerHTML =
        "";


    selectedMediaPreview.style.display =
        "none";


    clearPreviewUrl();
}


function clearPreviewUrl() {

    if (mediaPreviewUrl !== null) {

        URL.revokeObjectURL(
            mediaPreviewUrl
        );

        mediaPreviewUrl =
            null;
    }
}


async function sendSelectedMedia() {

    if (selectedMediaFile === null) {

        showMediaStatus(
            "Please select a file first",
            false
        );

        return;
    }


    sendMediaButton.disabled =
        true;


    sendMediaButton.innerText =
        "Sending...";


    let data =
        await uploadMedia(
            "send",
            selectedMediaFile,
            ""
        );


    sendMediaButton.disabled =
        false;


    sendMediaButton.innerText =
        "Send File";


    if (data === null) {

        return;
    }


    showMediaStatus(
        data.message,
        data.success
    );


    if (data.success === true) {

        clearSelectedMedia();


        await loadMessages();


        lastMessageSnapshot =
            null;
    }
}


async function uploadMedia(
    action,
    file,
    messageId
) {

    let fileData;


    try {

        fileData =
            await readFileData(
                file
            );


    } catch (error) {

        showMediaStatus(
            "The file could not be read",
            false
        );

        return null;
    }


    let mimeType =
        file.type;


    if (mimeType === "") {

        mimeType =
            "application/octet-stream";
    }


    let body =
        "action="
        + encodeURIComponent(action)

        + "&chatId="
        + encodeURIComponent(chatId)

        + "&senderId="
        + encodeURIComponent(userId)

        + "&messageId="
        + encodeURIComponent(messageId)

        + "&fileName="
        + encodeURIComponent(file.name)

        + "&mimeType="
        + encodeURIComponent(mimeType)

        + "&fileData="
        + encodeURIComponent(fileData);


    try {

        let response =
            await fetch(
                api + "/api/media",

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

        showMediaStatus(
            "Media could not be sent",
            false
        );

        return null;
    }
}


function readFileData(
    file
) {

    return new Promise(
        function (
            resolve,
            reject
        ) {

            let reader =
                new FileReader();


            reader.onload =
                function () {

                    resolve(
                        reader.result
                    );
                };


            reader.onerror =
                function () {

                    reject(
                        new Error(
                            "File read error"
                        )
                    );
                };


            reader.readAsDataURL(
                file
            );
        }
    );
}


function showMediaStatus(
    text,
    success
) {

    if (success === true) {

        messageStatus.style.color =
            "green";

    } else {

        messageStatus.style.color =
            "red";
    }


    messageStatus.innerText =
        text;
}


function renderAllMediaMessages() {

    if (mediaObserverRunning
            === true) {

        return;
    }


    mediaObserverRunning =
        true;


    let messageElements =
        messages.getElementsByClassName(
            "message"
        );


    for (let i = 0;
         i < messageElements.length;
         i++) {

        renderOneMediaMessage(
            messageElements[i]
        );
    }


    mediaObserverRunning =
        false;
}


function renderOneMediaMessage(
    messageElement
) {

    let textElement =
        messageElement.querySelector(
            ".message-text"
        );


    if (textElement === null) {

        return;
    }


    if (textElement.dataset.mediaReady
            === "true") {

        return;
    }


    let content =
        textElement.innerText;


    if (!content.startsWith(
            "__MEDIA__|"
    )) {

        return;
    }


    let media =
        parseMediaContent(
            content
        );


    if (media === null) {

        return;
    }


    textElement.dataset.mediaReady =
        "true";


    textElement.innerHTML =
        "";


    let mediaCard =
        document.createElement(
            "div"
        );


    mediaCard.className =
        "chat-media-card";


    let mediaUrl =
        api
        + "/api/media?file="
        + encodeURIComponent(
            media.storedFileName
        );


    if (media.mimeType.startsWith(
            "image/"
    )) {

        let imageLink =
            document.createElement(
                "a"
            );


        imageLink.href =
            mediaUrl;


        imageLink.target =
            "_blank";


        let image =
            document.createElement(
                "img"
            );


        image.className =
            "chat-media-image";


        image.src =
            mediaUrl;


        image.alt =
            media.originalFileName;


        imageLink.appendChild(
            image
        );


        mediaCard.appendChild(
            imageLink
        );


    } else if (
        media.mimeType.startsWith(
            "video/"
        )
    ) {

        let video =
            document.createElement(
                "video"
            );


        video.className =
            "chat-media-video";


        video.src =
            mediaUrl;


        video.controls =
            true;


        mediaCard.appendChild(
            video
        );


    } else if (
        media.mimeType.startsWith(
            "audio/"
        )
    ) {

        let audio =
            document.createElement(
                "audio"
            );


        audio.className =
            "chat-media-audio";


        audio.src =
            mediaUrl;


        audio.controls =
            true;


        mediaCard.appendChild(
            audio
        );


    } else {

        let fileBox =
            document.createElement(
                "div"
            );


        fileBox.className =
            "chat-file-box";


        let icon =
            document.createElement(
                "span"
            );


        icon.className =
            "chat-file-icon";


        icon.innerText =
            "📄";


        let information =
            document.createElement(
                "div"
            );


        information.className =
            "chat-file-information";


        let fileName =
            document.createElement(
                "span"
            );


        fileName.className =
            "chat-file-name";


        fileName.innerText =
            media.originalFileName;


        information.appendChild(
            fileName
        );


        fileBox.appendChild(
            icon
        );


        fileBox.appendChild(
            information
        );


        mediaCard.appendChild(
            fileBox
        );
    }


    let link =
        document.createElement(
            "a"
        );


    link.className =
        "chat-media-link";


    link.href =
        mediaUrl;


    link.target =
        "_blank";


    link.innerText =
        "Open "
        + media.originalFileName;


    mediaCard.appendChild(
        link
    );


    textElement.appendChild(
        mediaCard
    );


    hideTextEditButton(
        messageElement
    );


    if (messageElement.dataset.mine
            === "true") {

        addReplaceMediaButton(
            messageElement
        );
    }
}


function parseMediaContent(
    content
) {

    let fields =
        content.split("|");


    if (fields.length !== 4) {

        return null;
    }


    return {

        storedFileName:
            fields[1],

        originalFileName:
            fields[2],

        mimeType:
            fields[3]
    };
}


function hideTextEditButton(
    messageElement
) {

    let buttons =
        messageElement.querySelectorAll(
            ".message-buttons button"
        );


    for (let i = 0;
         i < buttons.length;
         i++) {

        if (buttons[i].innerText
                === "Edit") {

            buttons[i].style.display =
                "none";
        }
    }
}


function addReplaceMediaButton(
    messageElement
) {

    let buttonsBox =
        messageElement.querySelector(
            ".message-buttons"
        );


    if (buttonsBox === null) {

        return;
    }


    if (buttonsBox.querySelector(
            ".replace-media-button"
    ) !== null) {

        return;
    }


    let replaceButton =
        document.createElement(
            "button"
        );


    replaceButton.className =
        "replace-media-button";


    replaceButton.innerText =
        "Replace";


    replaceButton.addEventListener(
        "click",
        function () {

            selectReplacementFile(
                messageElement.dataset
                    .messageId
            );
        }
    );


    buttonsBox.appendChild(
        replaceButton
    );
}


function selectReplacementFile(
    messageId
) {

    let replacementInput =
        document.createElement(
            "input"
        );


    replacementInput.type =
        "file";


    replacementInput.style.display =
        "none";


    document.body.appendChild(
        replacementInput
    );


    replacementInput.addEventListener(
        "change",
        async function () {

            let file =
                replacementInput.files[0];


            if (file === undefined) {

                replacementInput.remove();

                return;
            }


            if (file.size === 0
                    || file.size > 5000000) {

                showMediaStatus(
                    "Replacement file must be between 1 byte and 5 MB",
                    false
                );

                replacementInput.remove();

                return;
            }


            let answer =
                confirm(
                    "Do you want to replace this media?"
                );


            if (answer === false) {

                replacementInput.remove();

                return;
            }


            showMediaStatus(
                "Replacing media...",
                true
            );


            let data =
                await uploadMedia(
                    "replace",
                    file,
                    messageId
                );


            replacementInput.remove();


            if (data === null) {

                return;
            }


            showMediaStatus(
                data.message,
                data.success
            );


            if (data.success === true) {

                await loadMessages();


                lastMessageSnapshot =
                    null;
            }
        }
    );


    replacementInput.click();
}


async function checkForMessageChanges() {

    try {

        let response =
            await fetch(
                api
                + "/api/messages?chatId="
                + encodeURIComponent(
                    chatId
                )
            );


        let data =
            await response.json();


        if (Array.isArray(data)
                === false) {

            return;
        }


        let newSnapshot =
            JSON.stringify(data);


        if (lastMessageSnapshot
                === null) {

            lastMessageSnapshot =
                newSnapshot;

            return;
        }


        if (lastMessageSnapshot
                !== newSnapshot) {

            lastMessageSnapshot =
                newSnapshot;


            await loadMessages();
        }


    } catch (error) {

        console.log(
            "Message update check failed"
        );
    }
}


let mediaMessageObserver =
    new MutationObserver(
        function () {

            renderAllMediaMessages();
        }
    );


mediaMessageObserver.observe(
    messages,

    {
        childList:
            true,

        subtree:
            true
    }
);


renderAllMediaMessages();


setInterval(
    checkForMessageChanges,
    3000
);


window.addEventListener(
    "beforeunload",
    function () {

        clearPreviewUrl();


        mediaMessageObserver.disconnect();
    }
);