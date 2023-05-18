package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data

// Obtiene los equipos inscritos en un torneo
@NamedQuery(name = "TeamsByTournamentId", query = "SELECT e.team FROM TournamentTeam e WHERE e.tournament.id = :tournamentId")

// Obtiene los torneos donde se encuentre inscrito un equipo con id "teamId"
@NamedQuery(name = "TournamentsByTeamId", query = "SELECT e.tournament FROM TournamentTeam e WHERE e.team.id = :teamId")
public class TournamentTeam {

    /*
     *  Id autogenerado que actua como clave primaria de la tabla TournamentTeam
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private long id;

    /*
     * Torneo "T" en el que se encuentra inscrito el equipo "E"
     */
    @OneToOne
    private Tournament tournament;

    /*
     * Referncia al team "E" inscrito en el torneo "T"
     */
    @OneToOne
    private Team team;

    @Column(nullable = true)
    private int puntuacion;
    @Column(nullable = true)
    private int victorias;
    @Column(nullable = true)
    private int empates;
    @Column(nullable = true)
    private int derrotas;

}
