package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="TOURNAMENT_TEAM")
public class Tournament_Team {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    @OneToOne
    private Tournament tournament;

    @OneToOne
    private Team team;

}
