package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data

// Obtiene los partidos en los que se encuentran el Team1 o el Team2
@NamedQuery(name = "MatchesWithTeamOneOrTeamTwo", query = "SELECT e FROM Match e WHERE e.team1.id = :teamId OR e.team2.id = :teamId")
public class Match {

    /*
     *  Id autogenerado que actua como clave primaria de la tabla TournamentTeam
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    /*
     * Referencia a message topic
     */
    @OneToOne
    private MessageTopic messageTopic;

    /*
     * Referencia al torneo en el que se encuentra este "Match"
     */
    @OneToOne
    private Tournament tournament;

    /*
     * Referencia a los equipos que conforman este "Match"
     */
    @OneToOne
    private Team team1;
    @OneToOne
    private Team team2;

    /*
     * Referencia al ganador del "Match"
     */
    @OneToOne
    private Team winner;

    /*
     * Resultado del partido
     */
    String result;

    /*
     * - Round Number: representa la fase actual del torneo.
     * En el caso de RR representa la jornada actual de la liga y en 
     * el caso de Simple Elimination, la fase (octavos, cuartos, semis..etc)
     * 
     * - Match Number: representa el numero de partido de la ronda actual.
     * En el caso de RR representaria el numero de partido de la jornada y 
     * en el caso de Simple Elimination, el numero de partido de la fase.
     */
    @Column(nullable = false)
    private int roundNumber;
    @Column(nullable = false)
    private int matchNumber;

    // Atributos para los distintos tipos de torneos

        // Round Robin
        /*
         * Indica si el partido ha terminado en empate
         */
        private Boolean draw;

}
