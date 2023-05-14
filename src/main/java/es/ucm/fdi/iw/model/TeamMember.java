package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data

// Consulta para obtener los Ids de los Users de un equipo
@NamedQuery(name = "MembersIdsByTeam", query = "SELECT e.userId FROM TeamMember e WHERE e.teamId = :teamid")
@NamedQuery(name = "AllCoachs", query = "SELECT e FROM TeamMember e WHERE e.isCoach = true")
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
