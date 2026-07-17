let api =
    "http://localhost:8080";


let profileUsername =
    document.getElementById(
        "profileUsername"
    );

let profileId =
    document.getElementById(
        "profileId"
    );

let profilePicture =
    document.getElementById(
        "profilePicture"
    );

let profileInput =
    document.getElementById(
        "profileInput"
    );

let savePictureBtn =
    document.getElementById(
        "savePictureBtn"
    );

let removePictureBtn =
    document.getElementById(
        "removePictureBtn"
    );

let pageMessage =
    document.getElementById(
        "pageMessage"
    );


let newUsername =
    document.getElementById(
        "newUsername"
    );

let changeUsernameBtn =
    document.getElementById(
        "changeUsernameBtn"
    );


let newUserId =
    document.getElementById(
        "newUserId"
    );

let idPassword =
    document.getElementById(
        "idPassword"
    );

let changeUserIdBtn =
    document.getElementById(
        "changeUserIdBtn"
    );


let currentPassword =
    document.getElementById(
        "currentPassword"
    );

let newPassword =
    document.getElementById(
        "newPassword"
    );

let repeatPassword =
    document.getElementById(
        "repeatPassword"
    );

let changePasswordBtn =
    document.getElementById(
        "changePasswordBtn"
    );


let darkMode =
    document.getElementById(
        "darkMode"
    );

let logoutBtn =
    document.getElementById(
        "logoutBtn"
    );

let deletePassword =
    document.getElementById(
        "deletePassword"
    );

let deleteAccountBtn =
    document.getElementById(
        "deleteAccountBtn"
    );


let currentImageData = "";
let selectedImageData = "";


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

    loadTheme();

    startPage();
}


profileInput.addEventListener(
    "change",
    previewProfilePicture
);


savePictureBtn.addEventListener(
    "click",
    saveProfilePicture
);


removePictureBtn.addEventListener(
    "click",
    removeProfilePicture
);


changeUsernameBtn.addEventListener(
    "click",
    changeUsername
);


changeUserIdBtn.addEventListener(
    "click",
    changeUserId
);


changePasswordBtn.addEventListener(
    "click",
    changePassword
);


darkMode.addEventListener(
    "change",
    changeTheme
);


logoutBtn.addEventListener(
    "click",
    logout
);


deleteAccountBtn.addEventListener(
    "click",
    deleteAccount
);


async function startPage() {

    await loadAccount();

    await loadProfilePicture();
}


async function loadAccount() {

    showMessage(
        "",
        false
    );


    try {

        let response =
            await fetch(
                api
                + "/api/account/settings?userId="
                + encodeURIComponent(userId)
            );


        let data =
            await response.json();


        if (data.success === false) {

            showMessage(
                data.message,
                false
            );

            return;
        }


        profileUsername.innerText =
            data.username;


        profileId.innerText =
            "User ID: " + data.id;


        newUsername.value =
            data.username;


        newUserId.value =
            data.id;


        if (currentImageData === "") {

            showProfileLetter(
                data.username
            );
        }


        saveUsername(
            data.username
        );


    } catch (error) {

        showMessage(
            "Server is not connected",
            false
        );
    }
}


async function changeUsername() {

    let username =
        newUsername.value.trim();


    if (username === "") {

        showMessage(
            "Please enter a new username",
            false
        );

        return;
    }


    changeUsernameBtn.disabled =
        true;

    changeUsernameBtn.innerText =
        "Saving...";


    let body =
        "action=changeUsername"
        + "&userId="
        + encodeURIComponent(userId)
        + "&newUsername="
        + encodeURIComponent(username);


    let data =
        await sendAccountRequest(
            body
        );


    if (data !== null) {

        showMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            saveUsername(
                username
            );

            await loadAccount();
        }
    }


    changeUsernameBtn.disabled =
        false;

    changeUsernameBtn.innerText =
        "Save Username";
}


async function changeUserId() {

    let newId =
        newUserId.value.trim();

    let password =
        idPassword.value;


    if (newId === ""
            || password === "") {

        showMessage(
            "Enter the new User ID and current password",
            false
        );

        return;
    }


    if (!/^[A-Za-z0-9_-]{1,40}$/.test(newId)) {

        showMessage(
            "User ID can only contain letters, numbers, underscore and dash",
            false
        );

        return;
    }


    if (newId.toLowerCase()
            === String(userId).toLowerCase()) {

        showMessage(
            "New User ID must be different",
            false
        );

        return;
    }


    let answer =
        confirm(
            "Change User ID from "
            + userId
            + " to "
            + newId
            + "?"
        );


    if (answer === false) {

        return;
    }


    changeUserIdBtn.disabled =
        true;

    changeUserIdBtn.innerText =
        "Changing...";


    let body =
        "oldUserId="
        + encodeURIComponent(userId)
        + "&newUserId="
        + encodeURIComponent(newId)
        + "&currentPassword="
        + encodeURIComponent(password);


    try {

        let response =
            await fetch(
                api
                + "/api/account/change-id",

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

            saveNewUserId(
                data.newUserId
            );


            userId =
                data.newUserId;


            newUserId.value =
                userId;


            idPassword.value =
                "";


            await loadAccount();

            await loadProfilePicture();
        }


    } catch (error) {

        showMessage(
            "Server is not connected",
            false
        );
    }


    changeUserIdBtn.disabled =
        false;

    changeUserIdBtn.innerText =
        "Change User ID";
}


async function changePassword() {

    let oldPassword =
        currentPassword.value;

    let password =
        newPassword.value;

    let repeat =
        repeatPassword.value;


    if (oldPassword === ""
            || password === ""
            || repeat === "") {

        showMessage(
            "Please complete all password fields",
            false
        );

        return;
    }


    if (password !== repeat) {

        showMessage(
            "New passwords do not match",
            false
        );

        return;
    }


    if (password.length < 8) {

        showMessage(
            "New password must have at least 8 characters",
            false
        );

        return;
    }


    changePasswordBtn.disabled =
        true;

    changePasswordBtn.innerText =
        "Changing...";


    let body =
        "action=changePassword"
        + "&userId="
        + encodeURIComponent(userId)
        + "&currentPassword="
        + encodeURIComponent(oldPassword)
        + "&newPassword="
        + encodeURIComponent(password);


    let data =
        await sendAccountRequest(
            body
        );


    if (data !== null) {

        showMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            currentPassword.value = "";
            newPassword.value = "";
            repeatPassword.value = "";
        }
    }


    changePasswordBtn.disabled =
        false;

    changePasswordBtn.innerText =
        "Change Password";
}


async function deleteAccount() {

    let password =
        deletePassword.value;


    if (password === "") {

        showMessage(
            "Enter your current password",
            false
        );

        return;
    }


    let answer =
        confirm(
            "Are you sure you want to delete your account?"
        );


    if (answer === false) {

        return;
    }


    let secondAnswer =
        confirm(
            "This action cannot be undone. Continue?"
        );


    if (secondAnswer === false) {

        return;
    }


    deleteAccountBtn.disabled =
        true;

    deleteAccountBtn.innerText =
        "Deleting...";


    let body =
        "action=deleteAccount"
        + "&userId="
        + encodeURIComponent(userId)
        + "&currentPassword="
        + encodeURIComponent(password);


    let data =
        await sendAccountRequest(
            body
        );


    if (data !== null) {

        showMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            clearLoginData();


            setTimeout(
                function () {

                    window.location.href =
                        "Login.html";
                },

                1000
            );


            return;
        }
    }


    deleteAccountBtn.disabled =
        false;

    deleteAccountBtn.innerText =
        "Delete Account";
}


async function sendAccountRequest(
    body
) {

    try {

        let response =
            await fetch(
                api
                + "/api/account/settings",

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


function saveNewUserId(
    newId
) {

    if (localStorage.getItem(
        "userId"
    ) !== null) {

        localStorage.setItem(
            "userId",
            newId
        );
    }


    if (sessionStorage.getItem(
        "userId"
    ) !== null) {

        sessionStorage.setItem(
            "userId",
            newId
        );
    }
}


function saveUsername(
    username
) {

    if (localStorage.getItem(
        "userId"
    ) !== null) {

        localStorage.setItem(
            "username",
            username
        );
    }


    if (sessionStorage.getItem(
        "userId"
    ) !== null) {

        sessionStorage.setItem(
            "username",
            username
        );
    }
}


function changeTheme() {

    if (darkMode.checked === true) {

        document.body.classList.add(
            "dark-mode"
        );


        localStorage.setItem(
            "chatTheme",
            "dark"
        );

    } else {

        document.body.classList.remove(
            "dark-mode"
        );


        localStorage.setItem(
            "chatTheme",
            "light"
        );
    }
}


function loadTheme() {

    let theme =
        localStorage.getItem(
            "chatTheme"
        );


    if (theme === "dark") {

        darkMode.checked =
            true;

        document.body.classList.add(
            "dark-mode"
        );

    } else {

        darkMode.checked =
            false;

        document.body.classList.remove(
            "dark-mode"
        );
    }
}


function logout() {

    let answer =
        confirm(
            "Do you want to logout?"
        );


    if (answer === false) {

        return;
    }


    clearLoginData();


    window.location.href =
        "Login.html";
}


function clearLoginData() {

    localStorage.removeItem(
        "userId"
    );

    localStorage.removeItem(
        "username"
    );

    sessionStorage.removeItem(
        "userId"
    );

    sessionStorage.removeItem(
        "username"
    );
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


async function loadProfilePicture() {

    try {

        let response =
            await fetch(
                api
                + "/api/account/profile-picture?userId="
                + encodeURIComponent(userId)
            );


        let data =
            await response.json();


        if (data.success === false) {

            currentImageData = "";

            showProfileLetter(
                profileUsername.innerText
            );

            return;
        }


        currentImageData =
            data.imageData;


        if (currentImageData !== "") {

            showProfileImage(
                currentImageData
            );

        } else {

            showProfileLetter(
                profileUsername.innerText
            );
        }


    } catch (error) {

        showMessage(
            "Profile picture could not be loaded",
            false
        );
    }
}


function previewProfilePicture() {

    let file =
        profileInput.files[0];


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


        profileInput.value = "";

        selectedImageData = "";

        return;
    }


    if (file.size > 1000000) {

        showMessage(
            "Profile picture must be smaller than 1 MB",
            false
        );


        profileInput.value = "";

        selectedImageData = "";

        return;
    }


    let reader =
        new FileReader();


    reader.onload =
        function () {

            selectedImageData =
                reader.result;


            showProfileImage(
                selectedImageData
            );


            showMessage(
                "Picture selected. Click Save Picture.",
                true
            );
        };


    reader.readAsDataURL(
        file
    );
}


async function saveProfilePicture() {

    if (selectedImageData === "") {

        showMessage(
            "Please select a picture first",
            false
        );

        return;
    }


    savePictureBtn.disabled =
        true;

    savePictureBtn.innerText =
        "Saving...";


    let body =
        "userId="
        + encodeURIComponent(userId)
        + "&action=save"
        + "&imageData="
        + encodeURIComponent(
            selectedImageData
        );


    let data =
        await sendProfileRequest(
            body
        );


    if (data !== null) {

        showMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            currentImageData =
                selectedImageData;

            selectedImageData = "";

            profileInput.value = "";


            showProfileImage(
                currentImageData
            );
        }
    }


    savePictureBtn.disabled =
        false;

    savePictureBtn.innerText =
        "Save Picture";
}


async function removeProfilePicture() {

    if (currentImageData === ""
            && selectedImageData === "") {

        showMessage(
            "There is no profile picture to remove",
            false
        );

        return;
    }


    let answer =
        confirm(
            "Do you want to remove your profile picture?"
        );


    if (answer === false) {

        return;
    }


    removePictureBtn.disabled =
        true;

    removePictureBtn.innerText =
        "Removing...";


    let body =
        "userId="
        + encodeURIComponent(userId)
        + "&action=remove";


    let data =
        await sendProfileRequest(
            body
        );


    if (data !== null) {

        showMessage(
            data.message,
            data.success
        );


        if (data.success === true) {

            currentImageData = "";

            selectedImageData = "";

            profileInput.value = "";


            showProfileLetter(
                profileUsername.innerText
            );
        }
    }


    removePictureBtn.disabled =
        false;

    removePictureBtn.innerText =
        "Remove Picture";
}


async function sendProfileRequest(
    body
) {

    try {

        let response =
            await fetch(
                api
                + "/api/account/profile-picture",

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


function showProfileImage(
    imageData
) {

    profilePicture.innerText = "";


    profilePicture.style.backgroundImage =
        "url('" + imageData + "')";
}


function showProfileLetter(
    username
) {

    profilePicture.style.backgroundImage =
        "none";


    if (username === undefined
            || username === null
            || username === "") {

        profilePicture.innerText =
            "?";

        return;
    }


    profilePicture.innerText =
        username
            .charAt(0)
            .toUpperCase();
}