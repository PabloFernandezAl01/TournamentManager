
// Obtiene el boton de invitar jugador
let inviteBtn = document.getElementById("invite-btn");

let username = document.getElementById("nombre");

inviteBtn.onclick = (event) => {
    event.preventDefault();

    go(inviteBtn.parentNode.action, 'POST', {
      message: username.value
    })
      .then(d => {
        console.log("Jugador invitado", d);
        username.value = ""; // Borra el contenido tras invitar al usuario
      })
      .catch(e => console.log("La invitación no ha podido enviarse", e))
  }

