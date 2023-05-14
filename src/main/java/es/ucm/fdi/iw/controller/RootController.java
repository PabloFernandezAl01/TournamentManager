package es.ucm.fdi.iw.controller;
import es.ucm.fdi.iw.model.Tournament.TournamentStatus;
import es.ucm.fdi.iw.model.TeamMember;
import es.ucm.fdi.iw.model.Tournament;
import es.ucm.fdi.iw.model.User;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
 * Por mejorar:
 *  - Aleatoriedad en los emparejamientos de equipos
 *  - Ver si tiene sentido la creacion de equipos cada vez que se hace un GET
 *  - ¿ Porque hay logica de torneos aqui y no en TournamentController?
 */

/**
 * Non-authenticated requests only.
 */
@Controller
public class RootController {

    /*
     * Estructura para manejar la informacion de un torneo
     */
    @Data
    @AllArgsConstructor
    public static class TData {

        int teamsIn; // Equipos inscritos en el torneo
        int capacity; // Maximo numero de equipos del torneo
        TournamentStatus status; // Estado actual del torneo

        // Indica si el usuario tiene alguno de sus equipos inscrito en el torneo
        // Independientemente de si el user es coach o no
        boolean userWithTeamIn;
    }

    /*
     * Clase para manejar un torneo (par {Tournament, TData})
     */
    @Data
    @AllArgsConstructor
    public static class Tourney {
        Tournament t; // Torneo
        TData data; // Y su informacion

        public boolean isFull() {
            return data.teamsIn >= data.capacity;
        }

        public boolean isJoinable() {
            return !isFull() && !data.userWithTeamIn;
        }
    }

    /*
     * Proporciona acceso a la base de BD desde cualquier parte del controlador
     */
    @Autowired
    private EntityManager entityManager;

    /*
     * Herramienta para encriptar contraseñas
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
	 * Encodes a password, so that it can be saved for future checking. Notice
	 * that encoding the same password multiple times will yield different
	 * encodings, since encodings contain a randomly-generated salt.
	 * 
	 * @param rawPassword to encode
	 * @return the encoded password (typically a 60-character string)
	 *         for example, a possible encoding of "test" is
	 *         {bcrypt}$2y$12$XCKz0zjXAP6hsFyVc8MucOzx6ER6IsC1qo5zQbclxhddR1t6SfrHm
	 */
	public String encodePassword(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

    @GetMapping("/")
    public String homepage(Model model) {
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/create")
    public String create(Model model, String exception) {
        model.addAttribute("create", "active");
        return "create";
    }

    @GetMapping("/join")
    @Transactional
    public String join(Model model, HttpSession session) {
        model.addAttribute("join", "active");

        // Obtiene los torneos que no han empezado (filterBy NOT STARTED)
        List<Tourney> tournaments = getTournamentsFilterByStatus(session, TournamentStatus.NOT_STARTED);

        // Añande los torneos al modelo
        model.addAttribute("Tournaments", tournaments);

        // Marca un flag indicando si el usuario es coach o no
        // (Solo los coach puede inscribir a sus equipos en torneos)
        model.addAttribute("IsCoach", isCoach(session));

        return "join";
    }

    @GetMapping("/ongoing")
    @Transactional
    public String ongoing(Model model, HttpSession session) {
        model.addAttribute("ongoing", "active");

        // Obtiene los torneos que han empezado (filterBy ON GOING)
        List<Tourney> tournaments = getTournamentsFilterByStatus(session, TournamentStatus.ON_GOING);

        // Añande los torneos al modelo
        model.addAttribute("Tournaments", tournaments);

        // Añande un flag indicando si el usuario es coach
        model.addAttribute("IsCoach", isCoach(session));
    
        return "ongoing";
    }

    @GetMapping("/record")
    public String record(Model model, HttpSession session) {
        model.addAttribute("record", "active");

        // Obtiene los torneos que han terminado (filterBy FINISHED)
        List<Tourney> tournaments = getTournamentsFilterByStatus(session, TournamentStatus.FINISHED);

        // Añande los torneos al modelo
        model.addAttribute("Tournaments", tournaments);

        return "record";
    }

    @GetMapping("/register")
    public String register(Model model) {
        return "register";
    }

    /*
     * Registra al usuario en la DB
     */
    @PostMapping("/register")
    @Transactional
    public RedirectView register(@ModelAttribute User registered, Model model) throws IOException {

        // Lo marca como activo y establece el rol del usuario
        registered.setEnabled(true); 
        registered.setRoles("USER");

        // Encripta la constraseña introducida por el usuario
        String encodedPassword = encodePassword(registered.getPassword());
        registered.setPassword(encodedPassword); 

        // Inicializa el valor de dinero obtenido y los reportes recibidos
        registered.setEarned(0);
        registered.setReports(0);

        // Añade/modifica la base de datos
        entityManager.persist(registered);

        // Sincroniza el contexto de persistencia con la DB
        entityManager.flush();

        // Redirige la vista a LOGIN ya que despues de registrarse tiene sentido iniciar sesion
        return new RedirectView("/login");
    }   

    /*
     * Filtra la lista de torneos para crear una nueva lista solo con los torneos
     * que coincidan con el estado "estatus" y su informacion clave para el HTML
     */
    private List<Tourney> getTournamentsFilterByStatus(HttpSession session, TournamentStatus status) {

        List<Tourney> tourneys = new ArrayList<>();

        // Consulta a la DB para obtener todos los torneos
        List<Tournament> tournaments = entityManager.createNamedQuery("AllTournaments", Tournament.class).getResultList();

        for (Tournament t : tournaments) {
            if (t.getStatus() == status) {

                int nTeams = getNumberOfTeamsInTournament(t);

                // Se crea el objeto con la informacion del torneo para añadirlo a la lista
                TData data = new TData(nTeams, t.getMaxTeams(), t.getStatus(), isMyTeamInTournament(session, t.getId()));

                tourneys.add(new Tourney(t, data));

            }
        }

        return tourneys;

    }

    /*
     * Comprueba si el usuario tiene alguno de sus equipos inscritos en el torneo T
     */
    private boolean isMyTeamInTournament(HttpSession session, long tournamentId) {

        User user = (User) session.getAttribute("u");

        // Obtiene todos los Ids de los equipos inscritos en el torneo T
        List<Long> teamsIds = entityManager.createNamedQuery("TeamsIdsByTournament", Long.class)
                                                .setParameter("tournamentid", tournamentId).getResultList();

        for (Long teamId : teamsIds) {

            // Por cada Id de equipo, obtiene sus integrantes
            List<Long> usersIdsInTeam = entityManager.createNamedQuery("MembersIdsByTeam", Long.class)
                                                                  .setParameter("teamid", teamId).getResultList();
            
            // Comprueba si algun Id de integrante del equipo concide con el Id del usuario
            for (Long u : usersIdsInTeam) {
                if (u == user.getId()) {
                    return true;
                }
            }
        }

        return false;                                

    }
    
    /*
     * Devuelve el numero de equipos inscritos en un torneo T
     */
    private int getNumberOfTeamsInTournament(Tournament t) {

        // Consulta el tamaño de la lista devuelta por la consutla
        int nTeams = entityManager.createNamedQuery("TeamsIdsByTournament", Long.class)
        .setParameter("tournamentid", t.getId()).getResultList().size();

        return nTeams;
    }

    private boolean isCoach(HttpSession session) {

        // Obtiene la informacion del usuario de la sesion actual
        User user = (User) session.getAttribute("u");

        // Obtiene todos los coachs de todos los Teams existentes en la aplicacion
        List<TeamMember> coachs = entityManager.createNamedQuery("AllCoachs", TeamMember.class).getResultList();

        // Comprueba si el usuario es coach recorriendo todos
        // los coachs que existen y comparando sus Ids
        for (TeamMember t : coachs) {
            if (t.getUserId() == user.getId()) {
                return true;
            }
        }

        return false;

    }
}
