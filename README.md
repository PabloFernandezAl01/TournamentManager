# Tournament Manager


## Propuesta actualizada

El objetivo de la página web es ofrecer a los usuarios un creador y gestor de torneos . Además, mostrará el historial de torneos jugados y sus respectivos equipos ganadores.
En la página habrá distintos roles encargados de gestionar los torneos y la web:

* Administrador de la página web: Representa a los usuarios con la capacidad de modificar los datos necesarios sobre los torneos, resolver problemas, y consultar información oculta para el resto de roles.
* Coach del equipo: Representa a los usuarios con la capacidad de crear equipo, añadir jugadores a los equipos y apuntar a sus equipos a los torneos.
* Jugador del equipo: Representa a los usuarios con la capacidad de unirse a un equipo y jugar los torneos.

En cuanto a los tipos de torneos:

* Round Robin: También llamado Liga, este formato consiste en un todos contra todos en el que gana el equipo con más victorias.
* Eliminación simple: Consta de varias fases, depende del número de equipos apuntados. Para pasar a la siguiente fase, cada equipo tiene que ganar a su rival, emparejados aleatoriamente. Si pierde, se elimina del torneo.

En cuanto las vistas:

* Vista principal: Contiene enlaces para las vistas generales de la aplicación además de enlaces para visitar algunas webs con información y noticias del competitivo actual de varios juegos.
* Crear torneo: Formulario para crear un torneo nuevo (Nombre, Tipo, Fecha, Juego, Descripción)
* Unirse a un torneo: Lista de torneos por comenzar. Si el usuario es coach y está en algún equipo, puede inscribir a su equipo en el torneo. Si se llena, nadie más se podrá inscribir y el torneo desaparecerá de esta vista cuando comience.
* Torneos en curso: Lista de torneos celebrándose. Por cada torneo, cualquier usuario inscrito en ese torneo puede ver los Brackets (en caso de ser eliminación simple) o la tabla (en caso de ser Round Robin) e incluso hablar por el chat del torneo.
* Historial de torneo: Lista de torneos finalizados. Cualquier usuario que forme parte de los equipos que se enfrentan en ese partido puede ver los Brackets o tabla con los resultados.
* Equipos: Lista de equipos con todos los equipos del usuario donde en cada equipo aparecerán los jugadores y su coach. Además, si el usuario es coach del equipo, podrá añadir nuevos jugadores al equipo a través de su nombre de usuario.
* Administración: Vista accesible solo por administradores donde aparece una lista con los usuarios de la aplicación. Los administradores pueden ver el número de reportes de los usuarios, su perfil, sus mensajes e incluso deshabilitarlos.
* Perfil: Vista con la información del perfil del usuario. En concreto, nombre de usuario, nombre, apellido y monedas. Además, aparece una lista con los próximos partidos del usuario y su historial de partidos jugados.
* Torneo: Vista con la información del torneo. Dependiendo del tipo de torneo se mostrarán los Brackets o la tabla de clasificación. Además, también está el chat de torneo.
* Partido: Vista con la información de un partido del torneo. Esta vista es muy importante ya que en ella, ambos equipos marcarán el resultado del partido. Si no coincide, el resultado lo decidirá el administrador. Además, también está el chat de partido.

## Diagrama de la BD

El diagrama de la Base de Datos se puede ver en el PDF dentro del proyecto llamado "BD_Grupo03"

## Usuarios 

Los usuarios existentes, con sus roles y contraseñas se pueden ver en la imagen dentro del proyecto llamada "Esquema usuarios.png"

## Pruebas

Las pruebas que hemos realizado han sido siempre usando el archivo "import.sql" y pruebas manuales viendo la ejecución. No hemos podido realizar pruebas de karate ya que por alguna razón que desconocemos no nos funciona en nuestros ordenadores al parecer. El error que hemos obtenido siempre es el siguiente "karate.env system property was: null" y después de una larga búsqueda de documentación del error, de comprobar que la ruta del navegador estuviera correcta, de cambiar varios archivos de test y probar incluso con otros navegadores. No hemos conseguido que las pruebas se ejecuten de ninguna forma.

# Tournament Manager

## Propuesta actualizada

El objetivo de la página web es ofrecer a los usuarios un creador y gestor de torneos . Además, mostrará el historial de torneos jugados y sus respectivos equipos ganadores.
En la página habrá distintos roles encargados de gestionar los torneos y la web:

* Administrador de la página web: Representa a los usuarios con la capacidad de modificar los datos necesarios sobre los torneos, resolver problemas, y consultar información oculta para el resto de roles.
* Coach del equipo: Representa a los usuarios con la capacidad de crear equipo, añadir jugadores a los equipos y apuntar a sus equipos a los torneos.
* Jugador del equipo: Representa a los usuarios con la capacidad de unirse a un equipo y jugar los torneos.

En cuanto a los tipos de torneos:

* Round Robin: También llamado Liga, este formato consiste en un todos contra todos en el que gana el equipo con más victorias.
* Eliminación simple: Consta de varias fases, depende del número de equipos apuntados. Para pasar a la siguiente fase, cada equipo tiene que ganar a su rival, emparejados aleatoriamente. Si pierde, se elimina del torneo.

En cuanto las vistas:

* Vista principal: Contiene enlaces para las vistas generales de la aplicación además de enlaces para visitar algunas webs con información y noticias del competitivo actual de varios juegos.
* Crear torneo: Formulario para crear un torneo nuevo (Nombre, Tipo, Fecha, Juego, Descripción)
* Unirse a un torneo: Lista de torneos por comenzar. Si el usuario es coach y está en algún equipo, puede inscribir a su equipo en el torneo. Si se llena, nadie más se podrá inscribir y el torneo desaparecerá de esta vista cuando comience.
* Torneos en curso: Lista de torneos celebrándose. Por cada torneo, cualquier usuario inscrito en ese torneo puede ver los Brackets (en caso de ser eliminación simple) o la tabla (en caso de ser Round Robin) e incluso hablar por el chat del torneo.
* Historial de torneo: Lista de torneos finalizados. Cualquier usuario que forme parte de los equipos que se enfrentan en ese partido puede ver los Brackets o tabla con los resultados.
* Equipos: Lista de equipos con todos los equipos del usuario donde en cada equipo aparecerán los jugadores y su coach. Además, si el usuario es coach del equipo, podrá añadir nuevos jugadores al equipo a través de su nombre de usuario.
* Administración: Vista accesible solo por administradores donde aparece una lista con los usuarios de la aplicación. Los administradores pueden ver el número de reportes de los usuarios, su perfil, sus mensajes e incluso deshabilitarlos.
* Perfil: Vista con la información del perfil del usuario. En concreto, nombre de usuario, nombre, apellido y monedas. Además, aparece una lista con los próximos partidos del usuario y su historial de partidos jugados.
* Torneo: Vista con la información del torneo. Dependiendo del tipo de torneo se mostrarán los Brackets o la tabla de clasificación. Además, también está el chat de torneo.
* Partido: Vista con la información de un partido del torneo. Esta vista es muy importante ya que en ella, ambos equipos marcarán el resultado del partido. Si no coincide, el resultado lo decidirá el administrador. Además, también está el chat de partido.

## Diagrama de la BD

El diagrama de la Base de Datos se puede ver en el PDF dentro del proyecto llamado "BD_Grupo03"

## Usuarios 

Los usuarios existentes, con sus roles y contraseñas se pueden ver en la imagen dentro del proyecto llamada "Esquema usuarios.png"

## Pruebas

Las pruebas que hemos realizado han sido siempre usando el archivo "import.sql" y pruebas manuales viendo la ejecución. No hemos podido realizar pruebas de karate ya que por alguna razón que desconocemos no nos funciona en nuestros ordenadores al parecer. El error que hemos obtenido siempre es el siguiente "karate.env system property was: null" y después de una larga búsqueda de documentación del error, de comprobar que la ruta del navegador estuviera correcta, de cambiar varios archivos de test y probar incluso con otros navegadores. No hemos conseguido que las pruebas se ejecuten de ninguna forma.


## Contenido de la plantilla
- en [src/main/java/es/ucm/fdi/iw](https://github.com/manuel-freire/iw/tree/main/plantilla/src/main/java/es/ucm/fdi/iw) están los ficheros de configuración-mediante-código de la aplicación (ojo porque en otro sitio está el fichero principal de configuración-mediante-propiedades, [application.properties](https://github.com/manuel-freire/iw/blob/main/plantilla/src/main/resources/application.properties)):

    * **AppConfig.java** - configura LocalData (usado para gestionar subida y bajada de ficheros de usuario) y fichero de internacionalización (que debería llamarse `Messages_XX.properties`, donde `XX` es un código como `es` para español ó `en` para inglés; y vivir en el directorio [resources](https://github.com/manuel-freire/iw/tree/main/plantilla/src/main/resources).
    * **IwApplication.java** - punto de entrada de Spring Boot
    * **IwUserDetailsService.java** - autenticación mediante base de datos. Referenciado desde SecurityConfig.java. La base de datos se inicializa tras cada arranque desde el [import.sql](https://github.com/manuel-freire/iw/blob/main/plantilla/src/main/resources/import.sql), aunque tocando [application.properties](https://github.com/manuel-freire/iw/blob/main/plantilla/src/main/resources/application.properties) puedes hacer que se guarde y cargue de disco, ignorando el _import_.
    * **LocalData.java** - facilita guardar y devolver ficheros de usuario (es decir, que no forman parte de los fuentes de tu aplicación). Para ello colabora con AppConfig y usa el directorio especificado en [application.properties](https://github.com/manuel-freire/iw/blob/main/plantilla/src/main/resources/application.properties)
    * **LoginSuccessHandler.java** - añade una variable de sesión llamada `u` nada más entrar un usuario, con la información de ese usuario. Esta variable es accesible desde Thymeleaf con `${session.user}`, y desde cualquier _Mapping_ de controllador usando el argumento `HttpSession session`, y leyendo su valor vía `(User)session.getAttribute("u")`. También añade a la sesión algo de configuración para websockets (variables `ws` y `url`), que se escriben como JS en las cabeceras de las páginas en el fragmento [head.html](https://github.com/manuel-freire/iw/blob/main/plantilla/src/main/resources/templates/fragments/head.html).
    * **SecurityConfig.java** - establece la configuración de seguridad. Modifica su método `configure` para decir quién puede hacer qué, mediante `hasRole` y `permitAll`. 
    * **StartupConfig.java** - se ejecuta nada más lanzarse la aplicación. En la plantilla sólo se usa para inicializar la `debug` a partir del [application.properties](https://github.com/manuel-freire/iw/blob/main/plantilla/src/main/resources/application.properties), accesible desde Thymeleaf mediante `${application.debug}`
    * **WebSocketConfig.java** - configura uso de websockets
    * **WebSocketSecurityConfig.java** - seguridad para websockets

- en [src/main/java/es/ucm/fdi/iw/controller](https://github.com/manuel-freire/iw/tree/main/plantilla/src/main/java/es/ucm/fdi/iw/controller) hay 3 controladores:

  * **RootController.java** - para usuarios que acaban de llegar al sitio, gestiona `/` y `/login`
  * **AdminController.java** - para administradores, gestionando todo lo que hay bajo `/admin`. No hace casi nada, pero sólo pueden llegar allí los que tengan rol administrador (porque así lo dice en SecurityConfig.config)
  * **UserControlller.java** - para usuarios registrados, gestionando todo lo que hay bajo `/user`. Tiene funcionalidad útil para construir páginas:
  
    + Un ejemplo de método para gestionar un formulario de cambiar información del usuario (bajo `@PostMapping("/{id}")`)
    + Puede devolver imágenes de avatar, y permite también subirlas. Ver métodos `getPic` (bajo `@GetMapping("{id}/pic")`) y `postPic` (bajo `@PostMapping("{id}/pic")`)
    + Puede gestionar también peticiones AJAX (= que no devuelven vistas) para consultar mensajes recibidos, consultar cuántos mensajes no-leídos tiene ese usuario, y enviar un mensaje a ese usuario (`retrieveMessages`, `checkUnread` y `postMsg`, respectivamente). Esta última función también envía el mensaje via websocket al usuario, si es que está conectado en ese momento.
    
- en [src/main/resources](https://github.com/manuel-freire/iw/tree/main/plantilla/src/main/resources) están los recursos no-de-código-de-servidor, y en particular, las vistas, los recursos web estáticos, el contenido inicial de la BBDD, y las propiedades generales de la aplicación.

  * **static/**  - contiene recursos estáticos web, como ficheros .js, .css, ó imágenes que no cambian
  
    - **js/stomp.js** - necesario para usar STOMP sobre websockets (que es lo que usaremos para enviar y recibir mensajes)
    - **js/iw.js** - configura websockets, y contiene funciones de utilidad para gestionar AJAX y previsualización de imágenes
    - **js/ajax-demo.js** - ejemplos (usados desde [user.html](https://github.com/manuel-freire/iw/blob/main/plantilla/src/main/resources/templates/user.html)) de AJAX, envío y recepción de mensajes por websockets, y previsualización de imágenes

  * **templates/** - contiene vistas, y fragmentos de vista (en `templates/fragments`)
  
    - **fragments/head.html** - para incluir en el `<head>` de tus páginas. Incluída desde  
    - **fragments/nav.html** - para incluir al comienzo del `<body>`, contiene una navbar. *Cambia los contenidos* para que tengan sentido para tu aplicación.    
    - **fragments/footer.html** - para incluir al final del `<body>`, con un footer. *Cambia su contenido visual*, pero ten en cuenta que es donde se cargan los .js de bootstrap, además de `stomp.js` e `iw.js`.
    - **error.html** - usada cuando se producen errores. Tiene un comportamiento muy distinto cuando la aplicación está en modo `debug` y cuando no lo está. 
    - **user.html** - vista de usuario. Debería mostrar información sobre un usuario, y posiblemente formularios para modificarle, pero en la plantilla se usa para demostrar funcionamiento de AJAX y websockets, en conjunción con `static/js/ajax-demo.js`. Deberías, lógicamente, *cambiar su contenido*.
  
  * **application.properties** - contiene la configuración general de la aplicación. Ojo porque ciertas configuraciones se hacen en los ficheros `XyzConfig.java` vistos anteriormente. Por ejemplo, qué roles pueden acceder a qué rutas se configura desde `SecurityConfig.java`.
  * **import.sql** - contiene código SQL para inicializar la BBDD. La configuración inicial hace que la BBDD se borre y reinicialice a cada arranque, lo cual es útil para pruebas. Es posible cambiarla para que la BBDD persista entre arraques de la aplicación, y se ignore el `import.sql`.
    
