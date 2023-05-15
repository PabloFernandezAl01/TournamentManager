function mostrarDescripcion() {

    var opciones = document.getElementById("option");
    var text = document.getElementById("tournament-type");

    let simple = `Consta de varias fases, depende del número de equipos apuntados. Para pasar a la siguiente fase,
      cada equipo tiene que ganar a su rival, emparejados aleatoriamente. Si pierde, se elimina del torneo.`;

    let double = `Al igual que Eliminación Simple, consta de varias fases, depende del número de equipos apuntados.
      Para pasar a la siguiente fase, cada equipo tiene que ganar a su rival, emparejados aleatoriamente.
      Si pierde, pasa a un bracket inferior en el que tendrá que jugar contra equipos que también hayan perdido.
      Si se pierde en este bracket, se elimina del torneo.`;

    let rr = `También llamado Round Robin, este formato consiste en un todos contra todos en el que gana el equipo 
      con más victorias tras una serie de jornadas.`;

    switch(opciones.value) {
      case "1":
        text.innerHTML = simple;
        break;
      case "2":
        text.innerHTML = double;
      break;
      case "3":
        text.innerHTML = rr;
        break;
      default:
        text.innerHTML = "";
        break;
    }

}