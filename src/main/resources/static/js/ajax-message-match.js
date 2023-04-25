//ENVIAR MENSAJE
let chatButton = document.getElementById("sendButton");
chatButton.onclick = (e) => {
  e.preventDefault();
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
  console.log("rendering: ", msg);
  return `<div>${msg.from} @${msg.sent}: ${msg.text}</div>`;
}

// pinta mensajes viejos al cargarse, via AJAX
let messageDiv = document.getElementById("mensajes");
go(config.rootUrl + "/user/received", "GET").then(ms =>
  ms.forEach(m => messageDiv.insertAdjacentHTML("beforeend", renderMsg(m))));

// y aquí pinta mensajes según van llegando
if (ws.receive) {
  const oldFn = ws.receive; // guarda referencia a manejador anterior
  ws.receive = (m) => {
      oldFn(m); // llama al manejador anterior
      messageDiv.insertAdjacentHTML("beforeend", renderMsg(m));
  }
}
