package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
public class TeamMember {

    /*
     * Id autogenerado que actua como clave primaria de la tabla TeamMember
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    /*
     * Id del "User"
     */
    @Column(nullable = false)
    private Long userId;

    /*
     * Id del "Team" de "User"
     */
    @Column(nullable = false)
    private Long teamId;

    /*
     * Booleano que representa el usuario "User" es Coach del equipo "Team"
     */
    private Boolean isCoach;

}
