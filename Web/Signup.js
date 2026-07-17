let username = document.getElementById("username");

let userId = document.getElementById("userId");

let password = document.getElementById("password");

let repeatPassword =
    document.getElementById("repeatPassword");

let createBtn =
    document.getElementById("createBtn");

let message =
    document.getElementById("message");


createBtn.addEventListener("click", async function () {

    let name = username.value.trim();

    let id = userId.value.trim();

    let pass = password.value;

    let repeatPass = repeatPassword.value;


    message.style.color = "red";

    message.innerText = "";


    if (name === ""
        || id === ""
        || pass === ""
        || repeatPass === "") {

        message.innerText =
            "Please fill in all fields";

        return;
    }


    if (pass !== repeatPass) {

        message.innerText =
            "Passwords do not match";

        return;
    }


    if (pass.length < 8) {

        message.innerText =
            "Password must have at least 8 characters";

        return;
    }


    if (!/[A-Z]/.test(pass)) {

        message.innerText =
            "Password must have an uppercase letter";

        return;
    }


    if (!/[a-z]/.test(pass)) {

        message.innerText =
            "Password must have a lowercase letter";

        return;
    }


    if (!/[0-9]/.test(pass)) {

        message.innerText =
            "Password must have a number";

        return;
    }


    if (!/[!@%$#^&*]/.test(pass)) {

        message.innerText =
            "Password must have a special character";

        return;
    }


    if (pass.toLowerCase().includes(name.toLowerCase())) {

        message.innerText =
            "Password cannot contain username";

        return;
    }


    createBtn.disabled = true;

    createBtn.innerText =
        "Please wait...";


    let body =
        "id=" + encodeURIComponent(id)
        + "&username=" + encodeURIComponent(name)
        + "&password=" + encodeURIComponent(pass);


    try {

        let response = await fetch(
            "http://localhost:8080/api/signup",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/x-www-form-urlencoded"
                },

                body: body
            }
        );


        let data = await response.json();


        if (data.success === true) {

            message.style.color = "green";

            message.innerText =
                data.message;


            setTimeout(function () {

                window.location.href =
                    "Login.html";

            }, 1000);

        } else {

            message.innerText =
                data.message;
        }


    } catch (error) {

        message.innerText =
            "Server is not connected";

    } finally {

        createBtn.disabled = false;

        createBtn.innerText =
            "Create Account";
    }

});