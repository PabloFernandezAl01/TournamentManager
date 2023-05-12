package es.ucm.fdi.iw.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
public class Image {

    Image(String filePath) {
        try {
            /*
             * Abre la imagen y lee todos sus bytes
             */
            data = Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            System.err.print(e.getMessage());
        }
    }

    byte[] getBytes() {
        return data;
    }

    /*
	 *  Id autogenerado que actua como clave primaria de la tabla Image
	 */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    /*
     * Informacion en bytes de la imagen
     */
    @Lob
    private byte[] data;
    
}
