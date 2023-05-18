package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data

// Obtiene los usuarios pertencientes a un Team
@NamedQuery(name = "MembersByTeam", query = "SELECT e.user FROM TeamMember e WHERE e.team.id = :teamId")

// Obtiene todos los jugadores de un equipo
@NamedQuery(name = "PlayersInTeam", query = "SELECT e.user FROM TeamMember e WHERE e.team.id = :teamId AND e.isCoach = false")

// Obtiene todos los coachs de un equipo
@NamedQuery(name = "CoachsInTeam", query = "SELECT e.user FROM TeamMember e WHERE e.team.id = :teamId AND e.isCoach = true")

// Obtiene todos los usuarios que sean coach de algun equipo
@NamedQuery(name = "AllCoachs", query = "SELECT e.user FROM TeamMember e WHERE e.isCoach = true")

// Obtiene el equipo de un partido en el que este el usuario
@NamedQuery(name = "MyTeamFromMatch", query = "SELECT m.team FROM TeamMember m WHERE (m.team.id = :team1 OR m.team.id = :team2) AND m.user.id = :userId")

// Obtiene todos los equipos en los que se encuentra el usuario
@NamedQuery(name = "AllMemberTeams", query = "SELECT t.team FROM TeamMember t WHERE t.user.id = :userId")

public class TeamMember {

    /*
     * Id autogenerado que actua como clave primaria de la tabla TeamMember
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    /*
     * Referencia al equipo "E" del miembro "M"
     */
    @OneToOne
    private Team team;

    /*
     * Referencia al miembro "M" del equipo "E"
     */
    @OneToOne
    private User user;

    /*
     * Booleano que representa el usuario "User" es Coach del equipo "Team"
     */
    private Boolean isCoach;

}
