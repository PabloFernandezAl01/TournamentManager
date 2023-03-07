Feature: login en servidor

#
#  Este test funciona, pero no es de buena educación martillear una API externa
#
#  Scenario: login malo en github
#    Given driver 'https://github.com/login'
#    And input('#login_field', 'dummy')
#    And input('#password', 'world')
#    When submit().click("input[name=commit]")
#    Then match html('.flash-error') contains 'Incorrect username or password.'
#

  Scenario: login malo en plantilla
    Given driver baseUrl + '/user/2'
    And input('#username', 'dummy')
    And input('#password', 'world')
    When submit().click(".loginButton")
    Then match html('.error') contains 'Error en nombre de usuario o contraseña'

  @register_d
  Scenario: Registrar correcto como d
    Given driver baseUrl + '/register'
    And input('#username', 'd')
    And input('#firstName', 'd')
    And input('#lastName', 'd')
    And input('#password', 'dd')
    When submit().click(".registerButton")
    Then waitForUrl(baseUrl + '/login')

  
  @login_d
  Scenario: login correcto como d
    Given driver baseUrl + '/login'
    And input('#username', 'd')
    And input('#password', 'dd')
    When submit().click(".loginButton")
    Then waitForUrl(baseUrl + '/user/\\d{3}')