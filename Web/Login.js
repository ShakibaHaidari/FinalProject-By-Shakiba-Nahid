let username = document.getElementById("username");
let password = document.getElementById("password");
let remember = document.getElementById("remember");
let loginBtn = document.getElementById("loginBtn");
let message = document.getElementById("message");


loginBtn.addEventListener("click", async function () {

    let name = username.value.trim();
    let pass = password.value;


    if (name === "" || pass === "") {

        message.innerText =
            "Please enter username and password";

        return;
    }


    message.innerText = "";

    loginBtn.disabled = true;
    loginBtn.innerText = "Please wait...";


    let body =
        "username=" + encodeURIComponent(name)
        + "&password=" + encodeURIComponent(pass);


    try {

        let response = await fetch(
            "http://localhost:8080/api/login",
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

            localStorage.removeItem("userId");
            localStorage.removeItem("username");

            sessionStorage.removeItem("userId");
            sessionStorage.removeItem("username");


            if (remember.checked === true) {

                localStorage.setItem(
                    "userId",
                    data.userId
                );

                localStorage.setItem(
                    "username",
                    data.username
                );

            } else {

                sessionStorage.setItem(
                    "userId",
                    data.userId
                );

                sessionStorage.setItem(
                    "username",
                    data.username
                );
            }


            window.location.href =
                "Home.html";

        } else {

            message.innerText =
                data.message;
        }


    } catch (error) {

        message.innerText =
            "Server is not connected";

    } finally {

        loginBtn.disabled = false;
        loginBtn.innerText = "Login";
    }

});