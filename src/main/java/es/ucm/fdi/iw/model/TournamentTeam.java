package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data

// Consulta para obtener los Ids de equipos inscritos en un torneo
@NamedQuery(name = "TeamsIdsByTournament", query = "SELECT e.teamId FROM TournamentTeam e WHERE e.tournamentId = :tournamentid")
public class TournamentTeam {

    /*
     *  Id autogenerado que actua como clave primaria de la tabla TournamentTeam
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private long id;

    /*
     * Id del "Team"
     */
    @Column(nullable = false)
    private Long teamId;

    /*
     * Id del "Tournament" en el que se encuentra el equipo "Team"
     */
    @Column(nullable = false)
    private Long tournamentId;

    /*
     * Booleano para indicar si el equipo "Team" es el ganador del torneo "Tournament"
     */
    private Boolean isWinner;

}
