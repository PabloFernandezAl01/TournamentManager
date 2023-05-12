package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
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
