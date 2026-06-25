function createAccount(){

let username =
document.getElementById("username").value;

let id =
document.getElementById("userId").value;

let password =
document.getElementById("password").value;

let repeat =
document.getElementById("repeatPassword").value;

if(username==="" || id==="" || password==="" || repeat===""){

alert("Please fill all fields");
}
else if(password !== repeat){
alert("Passwords are not same");
}
else{

alert("Account created");

window.location.href="index.html";
}
}