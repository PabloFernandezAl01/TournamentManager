

//ENVIAR MENSAJE
let chatButton = document.getElementById("sendButton");
chatButton.onclick = (event) => {
  event.preventDefault();
  go(chatButton.parentNode.action, 'POST', {
    message: document.getElementById("messageInput").value
  })
  .then(d => {
    console.log("message sent", d);
    document.getElementById("messageInput").value = ""; // borra el contenido cuando se manda el mensaje
  })
  .catch(e => console.log("message not sent", e))
}

// cómo pintar 1 mensaje (devuelve html que se puede insertar en un div)
function renderMsg(msg) {
  if(!msg.iamSender)
    return `
            <div class="others_message">
                <span class="date">${msg.sent}</span>
                ${msg.from} (${msg.fromTeam}): ${msg.text}
            </div>
          `;
  
  return `
          <div class="my_message">
              <span class="date">${msg.sent}</span>
              ${msg.from} (${msg.fromTeam}): ${msg.text}
          </div>
        `;
}


// pinta mensajes viejos al cargarse, via AJAX
let messageDiv = document.getElementById("mensajes");
go(config.rootUrl + "/user/received", "GET")
.then(messages =>
  messages.forEach(message => messageDiv.insertAdjacentHTML("beforeend", renderMsg(message)))
);

// y aquí pinta mensajes según van llegando
if (ws.receive) {
  const oldFn = ws.receive; // guarda referencia a manejador anterior
  ws.receive = (message) => {
      oldFn(message); // llama al manejador anterior
      messageDiv.insertAdjacentHTML("beforeend", renderMsg(message));
  }
}
