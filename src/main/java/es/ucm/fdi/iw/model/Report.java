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
     * Id del "User" que ha recibido el reporte
     */
    private Long userId;

    /*
     * Descripcion del reporte
     */
    private String description;

    /*
     * Id del mensaje causa del reporte
     */
    private Long messageId;

}
