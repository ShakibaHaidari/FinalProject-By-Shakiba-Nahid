document.getElementById("createAccountBtn").addEventListener("click", function () {

    let username = document.getElementById("username").value;
    let userId = document.getElementById("userId").value;
    let password = document.getElementById("password").value;
    let repeatPassword = document.getElementById("repeatPassword").value;

    if (username === "" || userId === "" || password === "" || repeatPassword === "") {
        alert("Please fill in all fields.");
        return;
    }

    if (password !== repeatPassword) {
        alert("Passwords do not match.");
        return;
    }

    alert("Account created successfully!");

    window.location.href = "Login.html";

});