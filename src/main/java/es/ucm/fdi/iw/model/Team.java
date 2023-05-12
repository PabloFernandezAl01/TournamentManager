package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
public class Team  {

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
     * Id como referencia a la imagen
     */
    private Long imageId;

}
