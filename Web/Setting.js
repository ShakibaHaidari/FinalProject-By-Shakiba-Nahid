function logout(){
alert("Logout");
window.location.href="index.html";
}
function deleteAccount(){
let result = confirm("Delete account?");
if(result){
alert("Account deleted");
}
}