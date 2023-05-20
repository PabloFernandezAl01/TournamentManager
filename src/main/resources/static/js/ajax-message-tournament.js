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
const o = document.querySelector(".tournamentButton");
const tournamentId = o.id;
let messageDiv = document.getElementById("mensajes");
config.currentChat = tournamentId;
go(config.rootUrl + "/user/rcvMsg/tournament/" + tournamentId, "GET")
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

  // messageDiv.addEventListener("click", function (event) {
  //   // Verificar si el elemento clickeado es un mensaje
  //   const clickedElement = event.target;
  //   if (clickedElement.classList.contains("others_message")) {

  //     // Mostrar la ventana modal correspondiente
  //     const modal = document.getElementById('myModal');
  //     modal.style.display = "block";
  //     var span = document.getElementsByClassName("close")[0];
  //     span.onclick = function () {
  //       modal.style.display = "none";
  //     }
  //     window.onclick = function (event) {
  //       if (event.target == modal) {
  //         modal.style.display = "none";
  //       }
  //     }
  //   }
  // });
