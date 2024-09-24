'use strict'

let usernamePage=document.querySelector('#username-page');
let chatPage=document.querySelector('#chat-page');
let usernameForm=document.querySelector('#usernameForm');
let messageForm=document.querySelector('#messageForm');
let messageInput=document.querySelector('#message');
let messageArea=document.querySelector('#messageArea');
let connectingElement=document.querySelector('.connecting');

let stompClient=null;
let username=null;

let colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event){
    //去除使用者名稱多於空格
    username=document.querySelector('#name').value.trim();
    if(username){
        //隱藏輸入姓名頁面  顯示聊天頁面
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');
        //創建webSocket連接
        let socket = new SockJS('/ws');
        //回傳stomp客戶端
        stompClient = Stomp.over(socket);

        stompClient.connect({},onConnected,onError);
    }
    event.preventDefault();
}

function onConnected(){
    //訂閱到此路徑
    stompClient.subscribe('/topic/public',onMessageReceived);
    //客戶端向/app/chat.addUser發送訊息
    stompClient.send('/app/chat.addUser',
        {},
        JSON.stringify({sender:username,type:'JOIN'})
    );
    connectingElement.classList.add('hidden');
}
usernameForm.addEventListener('submit',connect,true);

function  onError(){
    connectingElement.textContent='Could not connect to the server.';
    connectingElement.style.color='red';
}

function sendMessage(event) {
    let messageContent = messageInput.value.trim();
    if(messageContent && stompClient) {
        let chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}


function onMessageReceived(payload){
    let message=JSON.parse(payload.body);

    let messageElement=document.createElement('li');

    if(message.type==='JOIN'){
        messageElement.classList.add('event-message');
        message.content = message.sender+' joined!';
    }else if(message.type === 'LEAVE'){
        messageElement.classList.add('event-message');
        message.content=message.sender+' left!';
    }else{
        messageElement.classList.add('chat-message');

        let avatarElement=document.createElement('i');
        let avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);
        let usernameElement = document.createElement('span');
        let usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);

    }
    let textElement=document.createElement('p');
    let messageText=document.createTextNode(message.content);
    textElement.appendChild(messageText);
    messageElement.appendChild(textElement);
    messageArea.appendChild(messageElement);
    messageArea.scrollTop= message.scrollHeight;
}

function  getAvatarColor(messageSender){
    let hash=0;
    for(let i=0; i<messageSender.length; i++){
        hash=31*hash+messageSender.charCodeAt(i);
    }
    let index=Math.abs(hash%colors.length);
    return colors[index];
}
//連接
usernameForm.addEventListener('submit', connect, true)
//傳送訊息
messageForm.addEventListener('submit', sendMessage, true)
