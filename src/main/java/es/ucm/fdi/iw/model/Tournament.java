package es.ucm.fdi.iw.model;

import java.time.LocalDateTime;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
public class Tournament {

    public enum TournamentType {
        SINGLE_ELIMINATION,
        DOUBLE_ELIMINATION,
        ROUND_ROBIN
    }

    public enum TournamentStatus {
        NOT_STARTED,
        ON_GOING, 
        FINISHED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int maxTeams;
    @Column(nullable = false)
    private LocalDateTime date;

    private float entryPrice;

    @Column(nullable = false)
    private float prizePool;

    @Column(nullable = false)
    private TournamentType type;

    @Column(nullable = false)
    private String game;

    @Column(nullable = false)
    private TournamentStatus status;


    @OneToOne
    private Team winner;

    private int rounds;

    // Atributo blob para la imagen del team

    private String description;
}
