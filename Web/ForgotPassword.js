let api =
    "http://localhost:8080";


let username =
    document.getElementById(
        "username"
    );

let resetBtn =
    document.getElementById(
        "resetBtn"
    );

let message =
    document.getElementById(
        "message"
    );

let passwordBox =
    document.getElementById(
        "passwordBox"
    );

let temporaryPassword =
    document.getElementById(
        "temporaryPassword"
    );

let copyBtn =
    document.getElementById(
        "copyBtn"
    );


resetBtn.addEventListener(
    "click",
    resetPassword
);


username.addEventListener(
    "keydown",
    function (event) {

        if (event.key === "Enter") {

            resetPassword();
        }
    }
);


copyBtn.addEventListener(
    "click",
    copyPassword
);


async function resetPassword() {

    let name =
        username.value.trim();


    message.innerText = "";

    passwordBox.style.display =
        "none";


    if (name === "") {

        message.style.color =
            "red";

        message.innerText =
            "Please enter your username";

        return;
    }


    resetBtn.disabled = true;

    resetBtn.innerText =
        "Please wait...";


    let body =
        "username="
        + encodeURIComponent(name);


    try {

        let response =
            await fetch(
                api
                + "/api/forgot-password",

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

            message.style.color =
                "green";

            message.innerText =
                data.message;


            temporaryPassword.value =
                data.temporaryPassword;


            passwordBox.style.display =
                "block";

        } else {

            message.style.color =
                "red";

            message.innerText =
                data.message;
        }


    } catch (error) {

        message.style.color =
            "red";

        message.innerText =
            "Server is not connected";

    } finally {

        resetBtn.disabled = false;

        resetBtn.innerText =
            "Reset Password";
    }
}


async function copyPassword() {

    let password =
        temporaryPassword.value;


    if (password === "") {

        return;
    }


    try {

        await navigator.clipboard
            .writeText(password);


        copyBtn.innerText =
            "Copied";


    } catch (error) {

        temporaryPassword.select();

        document.execCommand(
            "copy"
        );


        copyBtn.innerText =
            "Copied";
    }


    setTimeout(
        function () {

            copyBtn.innerText =
                "Copy Password";
        },

        1200
    );
}