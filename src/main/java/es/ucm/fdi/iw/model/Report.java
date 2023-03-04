package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    @ManyToOne
    private User user;

    @OneToOne
    private Message message;

    private String reason;

    private String description;

}
