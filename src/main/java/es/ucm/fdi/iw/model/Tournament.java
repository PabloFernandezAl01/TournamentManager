package es.ucm.fdi.iw.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data

// Consulta para obtener todos los torneos
@NamedQuery(name = "AllTournaments", query = "SELECT t FROM Tournament t")



public class Tournament {

    /*
     * Tipos de torneo:
     *  - ELIMINACION SIMPLE: Por fases, una derrota elimina
     *  - ELIMINACION DOBLE: Upper y Loser bracket, dos derrotas eliminan
     *  - ROUND RODBIN: Liguilla, gana el que más puntos tenga al final
     */
    public enum TournamentType { SINGLE_ELIMINATION, DOUBLE_ELIMINATION, ROUND_ROBIN }

    /*
     * El torneo puede encontrarse en varios estados:
     *  - CANCELED: El torneo fue creado pero cancelando antes de su comienzo
     *  - NOT_STARTED: El torneo ha sido creado pero no ha empezado
     *  - ON_GOIND: El torneo fue creado y ha empezado
     *  - FINISHED: El torneo fue creado y ha terminado
     */
    public enum TournamentStatus { NOT_STARTED, ON_GOING, FINISHED }

     /*
     * Id autogenerado que actua como clave primaria de la tabla Tournament
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private long id;

    /*
     * Nombre del torneo, no NULL y unico
     */
    @Column(nullable = false, unique = true)
    private String name;


    // ------- ATRIBUTOS CON SENTIDO --------

    /*
     * - MaxTeams: Numero maximo de equipos que pueden entrar en el torneo
     * - Date: Fecha de comienzo
     * - EntryPrice: Precio de entrada al torneo
     * - PrizePool: premio del torneo
     * - Type: Tipo de torneo (single, double, RR)
     * - Status: Estado actual del torneo
     * - Game: Juego del torneo
     * - Description: Descripcion del torneo
     * - Image: Id como referencia a la imagen del torneo
    */
    private Integer maxTeams;
    private String date; // ¿String? TODO 
    private Double entryPrice;
    private Double prizePool;
    private String type;
    private TournamentStatus status;
    private String game;
    private String description;
    private Long imageId;

}
