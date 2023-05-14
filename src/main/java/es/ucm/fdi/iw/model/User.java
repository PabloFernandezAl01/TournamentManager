package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An authorized user of the system.
 * 
 * En esta clase hay comentarios para entender etiquetas y conceptos teoricos
 */

@Entity // Anotacion que sirve para marcar una clase como una entidad en un modelo de datos relacional
@Data // Etiqueda de lombok para generar automticamente getters & setters para los atributos de la clase
@NoArgsConstructor // Etiqueta de lombok que genera un constructor sin argumentos en la clase

@NamedQueries({ // Definicion de consultas (Independientemente del lugar donde se vayan a usar, deben estar definidas en las entidades)
    @NamedQuery(name = "User.byUsername", query = "SELECT u FROM User u " + "WHERE u.username = :username AND u.enabled = TRUE"),
    @NamedQuery(name = "User.hasUsername", query = "SELECT COUNT(u) " + "FROM User u " + "WHERE u.username = :username")
})

// Util para personalizar la tabla (Nombre, Esquema, Nombre de secuencia, etc). 
// Si la clase ya tiene la etiqueda @Entity, esta etiqueta no es necesaria ya que ya se esta mapeando en la DB.
@Table(name = "IWUser")

public class User implements Transferable<User.Transfer> { 

    // Roles del usuario
    public enum Role {
        USER, // normal users
        ADMIN, // admin users
    }

    private String roles; // split by ',' to separate roles

    /**
     * Checks whether this user has a given role.
     * 
     * @param role to check
     * @return true iff this user has that role.
     */
    public boolean hasRole(Role role) {
        String roleName = role.name();
        return Arrays.asList(roles.split(",")).contains(roleName);
    }

    /*
     * Id autogenerado que actua como clave primaria de la tabla IWUser
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private long id;

    /*
     * Username: nombre del usuario, unico y no NULL
     * Password: contraseña del usuario, no NULL
     */
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;

    /*
     * Datos del usuario:
     * - Nombre
     * - Primer apellido
     * - Enabled: Representa si el usuario esta habilitado en la aplicacion
     * - Coins: Cantidad de monedas acumuladas
     * - Report: Numero de reportes hacia este usuario
     * - Teams: Equipos a los que pertenece este usuario 
     */
    private String firstName;
    private String lastName;
    private boolean enabled;
    private int coins;
    private int reports;

    /*
     * Id como referencia a la imagen
     */
    private Long imageId;

    // Messages
    @OneToMany
    @JoinColumn(name = "sender_id")
    private List<Message> sent = new ArrayList<>();
    @OneToMany
    @JoinColumn(name = "recipient_id")
    private List<Message> received = new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Transfer {
        private long id;
        private String username;
        private int totalReceived;
        private int totalSent;
    }

    @Override
    public Transfer toTransfer() {
        return new Transfer(id, username, received.size(), sent.size());
    }

    @Override
    public String toString() {
        return username;
    }
}
