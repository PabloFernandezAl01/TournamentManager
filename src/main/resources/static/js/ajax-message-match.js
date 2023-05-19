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
  if (!msg.iamSender)
    return `
            <div class="others_message">
                <span class="date">${msg.sent}</span>
                <span class="teamFromMessage" >${msg.from} (${msg.fromTeam}): </span> ${msg.text}
            </div>
          `;

  return `
          <div class="my_message">
              <span class="date">${msg.sent}</span>
              ${msg.from} (${msg.fromTeam}): <span class="white"> ${msg.text} </span>
          </div>
        `;
}

config.currentChat = "";

function messagesForMatch(matchId) {
  config.currentChat = matchId;
  let messageDiv = document.getElementById("mensajes");
  messageDiv.innerHTML = "";
  go(config.rootUrl + "/user/rcvMsg/match/" + matchId, "GET")
    .then(messages =>
      messages.forEach(message => messageDiv.insertAdjacentHTML("beforeend", renderMsg(message)))
    );
}

document.querySelectorAll(".matchButton").forEach(o => {
  console.log("buscando mensjes para ", o);
  const matchId = o.id;
  console.log("... con id", matchId);
  o.addEventListener("click", (e) => messagesForMatch(matchId));
});


// pinta mensajes viejos al cargarse, via AJAX


// y aquí pinta mensajes según van llegando
if (ws.receive) {
  const oldFn = ws.receive; // guarda referencia a manejador anterior
  ws.receive = (message) => {
    oldFn(message); // llama al manejador anterior

    // solo deberias mostrar cosas que correspondan al chat abierto !!
    if (config.currentChat === message.matchId) {
      messageDiv.insertAdjacentHTML("beforeend", renderMsg(message));
    }
  }

  messageDiv.addEventListener("click", function (event) {
    // Verificar si el elemento clickeado es un mensaje
    const clickedElement = event.target;
    if (clickedElement.classList.contains("others_message")) {

      // Mostrar la ventana modal correspondiente
      const modal = document.getElementById('myModal');
      modal.style.display = "block";
      var span = document.getElementsByClassName("close")[0];
      span.onclick = function () {
        modal.style.display = "none";
      }
      window.onclick = function (event) {
        if (event.target == modal) {
          modal.style.display = "none";
        }
      }
    }
  });
}