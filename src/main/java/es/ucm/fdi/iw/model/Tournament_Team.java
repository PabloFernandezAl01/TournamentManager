package es.ucm.fdi.iw.model;

import javax.persistence.*;

import org.springframework.lang.Nullable;

import lombok.Data;

@Entity
@Data
@Table(name = "TOURNAMENT_TEAM")
public class Tournament_Team {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private long id;

    @OneToOne
    private Tournament tournament;

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
