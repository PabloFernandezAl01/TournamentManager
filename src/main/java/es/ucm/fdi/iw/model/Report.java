package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
public class Report {
    
    /*
	 *  Id autogenerado que actua como clave primaria de la tabla Report
	 */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    /*
     * Referencia al usuario que recibio el reporte
     */
    @ManyToOne
    private User user;

    /*
     * Mensaje que representa al reporte
     */
    @OneToOne
    private Message message;

    /*
     * Descripcion del reporte
     */
    private String description;

}
