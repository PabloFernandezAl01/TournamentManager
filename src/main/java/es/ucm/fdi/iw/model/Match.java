package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    @Column(nullable = false)
    private int roundNumber;

    @Column(nullable = false)
    private int matchNumber;

    @OneToOne
    private Team team1;

    @OneToOne
    private Team team2;
    
    private String topicId;

    @OneToOne
    private Tournament tournament;

    private String result;

    @OneToOne
    private Team winner;

    // Atributo blob para la imagen del team 1

    // Atributo blob para la imagen del team 2
    
}
