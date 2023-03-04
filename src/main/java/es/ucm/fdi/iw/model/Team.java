package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
public class Team  {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToOne
    private User coach;

    // Atributo blob para la imagen del team
    
}
