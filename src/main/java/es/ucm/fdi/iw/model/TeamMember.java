package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
public class TeamMember {

    public enum RoleInTeam {
        PLAYER,
        COACH
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    @OneToOne
    private Team team;

    @OneToOne
    private User user;

    private RoleInTeam role;

}
