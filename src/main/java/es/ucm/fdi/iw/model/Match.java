package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
public class Match {

    /*
     *  Id autogenerado que actua como clave primaria de la tabla TournamentTeam
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    /*
     * Id del torneo en el que se encuentra este "Match"
     */
    @Column(nullable = false)
    private long tournamentId;

    /*
     * Id de los equipos que conforman este "Match"
     */
    @Column(nullable = false)
    private long teamOne;
    @Column(nullable = false)
    private long teamTwo;

    // Atributos para los distintos tipos de torneos

        // Single & Double Elimination
        /*
         * Indica la fase del torneo en la que se encuentra este "Match"
         */
        @Column(nullable = false)
        private int round;

        // Round Robin
        /*
         * Indica si el partido ha terminado en empate
         */
        private Boolean draw;

}
