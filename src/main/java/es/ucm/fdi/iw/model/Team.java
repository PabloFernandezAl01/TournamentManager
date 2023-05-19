package es.ucm.fdi.iw.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import lombok.Data;

@Entity
@Data
public class Team  {

    /*
     * Constante para definir el numero de jugadores en un equipo
     */
    @Transient
    public static Integer MAX_PLAYERS_IN_TEAM = 5;

    /*
     * Id autogenerado que actua como clave primaria de la tabla Team
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    /*
     * Nombre del equipo, no NULL y unico
     */
    @Column(nullable = false, unique = true)
    private String name;

    /*
     * Lista de torneos ganados por el equipo
     */
    @OneToMany
    @JoinColumn(name = "winner_id")
    List<Tournament> wins = new ArrayList<>();

}
