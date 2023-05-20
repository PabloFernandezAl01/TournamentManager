Feature: flujo de la aplicacion

  Scenario: login
    Given login('UserTest', 'aa')
    Then waitForUrl(baseUrl + '/admin/')

  Scenario: Establecer ganador de partido
    Given login('UserTest', 'aa')
    When submit().click("#ongoingButton")
    When submit().click("#verTorneoButton")
    When submit().click("#verPartidoButton")
    When submit().click("#establecerGanadorButton")

  
 # Paso de inicio de sesión reutilizable
  Scenario: Login Step
    * def login(username, password) =
        """
        Given driver baseUrl + '/login'
        And input('#username', '{username}')
        And input('#password', '{password}')
        When submit().click(".loginButton")
        """