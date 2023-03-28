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

    private Integer maxTeams;

    private String date;

    private String creationDate;

    private String staringHour;

    private Double entryPrice;

    private Double prizePool;

    private String type;

    private String game;

    private String status;

    @OneToOne
    private Team winner;

    private int rounds;

    // Atributo blob para la imagen del team

    private String description;
}
