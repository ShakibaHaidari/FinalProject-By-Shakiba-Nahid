function sendMessage(){
let input = document.getElementById("message");
let text=input.value;
if(text===""){
return;
}
let box = document.getElementById("messages");
let div = document.createElement("div");
div.className="message my-message";
div.innerHTML=text;
box.appendChild(div);
input.value="";
}