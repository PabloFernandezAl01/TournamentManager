package es.ucm.fdi.iw.controller;
import es.ucm.fdi.iw.model.Tournament.TournamentStatus;
import es.ucm.fdi.iw.model.Team;
import es.ucm.fdi.iw.model.Tournament;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Match;
import es.ucm.fdi.iw.model.MessageTopic;
import es.ucm.fdi.iw.model.TeamMember;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

import org.springframework.stereotype.Controller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Non-authenticated requests only.
 */
@Controller
public class RootController {

    private static final Logger log = LogManager.getLogger(RootController.class);

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

        // Obtiene la informacion clave de los torneos
        List<Tourney> tournaments = getTournamentsData(session);

        // Añade los torneos al modelo
        model.addAttribute("Tournaments", tournaments);

        // Añande el estado de los torneos a filtrar
        model.addAttribute("Status", TournamentStatus.NOT_STARTED);

        // Marca un flag indicando si el usuario es coach o no
        // (Solo los coach puede inscribir a sus equipos en torneos)
        model.addAttribute("IsCoach", isCoach(session));
        
        return "join";
    }

    @GetMapping("/ongoing")
    @Transactional
    public String ongoing(Model model, HttpSession session) {
        model.addAttribute("ongoing", "active");

        // Obtiene la informacion clave de los torneos
        List<Tourney> tournaments = getTournamentsData(session);

        // Añande los torneos al modelo
        model.addAttribute("Tournaments", tournaments);

         // Añande el estado de los torneos a filtrar
         model.addAttribute("Status", TournamentStatus.ON_GOING);

        return "ongoing";
    }

    @GetMapping("/record")
    @Transactional
    public String record(Model model, HttpSession session) {
        model.addAttribute("record", "active");

        // Obtiene la informacion clave de los torneos
        List<Tourney> tournaments = getTournamentsData(session);

        // Añande los torneos al modelo
        model.addAttribute("Tournaments", tournaments);

         // Añande el estado de los torneos a filtrar
         model.addAttribute("Status", TournamentStatus.FINISHED);

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
        registered.setCoins(0);
        registered.setReports(0);

        // Añade/modifica la base de datos
        entityManager.persist(registered);

        // Redirige la vista a LOGIN ya que despues de registrarse tiene sentido iniciar sesion
        return new RedirectView("/login");
    }

    /*
     * Devuelve la informacion de todos los torneos
     */
    private List<Tourney> getTournamentsData(HttpSession session) {

        List<Tourney> tourneys = new ArrayList<>();
        List<Tournament> tournaments = new ArrayList<>();

        try {
            // Consulta a la DB para obtener todos los torneos
            tournaments = entityManager.createNamedQuery("AllTournaments", Tournament.class).getResultList();

        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }

        for (Tournament t : tournaments) {

            // Obtiene el numero de equipos inscritos en el torneo
            int nTeams = getNumberOfTeamsInTournament(t);

            if (LocalDate.now().isAfter(LocalDate.parse(t.getDate())) && (t.getStatus() == TournamentStatus.NOT_STARTED)) {
                    if (nTeams < t.getMaxTeams()) {
                        t.setStatus(TournamentStatus.CANCELED);
                    } else {
                        t.setStatus(TournamentStatus.ON_GOING);

                        // TODO: Hacer esto de manera asíncrona con WS
                        createMatches(t, session);
                        // else
                        // createLeagueMatches(tournament, session);
                    }
                }

            // Se crea el objeto con la informacion del torneo para añadirlo a la lista
            TData data = new TData(nTeams, t.getMaxTeams(), t.getStatus(), isMyTeamInTournament(session, t));

            tourneys.add(new Tourney(t, data));

        }

        return tourneys;
    }

    /*
     * Comprueba si el usuario tiene alguno de sus equipos inscritos en el torneo T
     */
    private boolean isMyTeamInTournament(HttpSession session, Tournament t) {

        User user = (User) session.getAttribute("u");

        List<Team> teams = new ArrayList<>();
        List<User> usersInTeam = new ArrayList<>();

        try {
            // Obtiene todos los equipos inscritos en el torneo T
            teams = entityManager.createNamedQuery("TeamsByTournamentId", Team.class)
                                         .setParameter("tournamentId", t.getId()).getResultList();
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }

        for (Team team : teams) {

            try {
                // Por cada Id de equipo, obtiene sus integrantes
                usersInTeam = entityManager.createNamedQuery("MembersByTeam", User.class)
                                                .setParameter("teamId", team.getId()).getResultList();
            } catch (IllegalArgumentException e) {
                log.error(e.getMessage());
            }
            
            // Comprueba si algun Id de integrante del equipo concide con el Id del usuario
            for (User u : usersInTeam) {
                if (u.getId() == user.getId()) {
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

        int nTeams = 0;

        try {
            // Consulta el tamaño de la lista devuelta por la consulta
            nTeams = entityManager.createNamedQuery("TeamsByTournamentId", Team.class)
                                    .setParameter("tournamentId", t.getId()).getResultList().size();

        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }

        return nTeams;
    }

    /*
     * Devuelve un bool indicando si el usuario es coach de algun equipo
     */
    private boolean isCoach(HttpSession session) {

        User user = null;
        List<User> coachs = new ArrayList<>();

        try {
            // Obtiene la informacion del usuario de la sesion actual
            user = (User) session.getAttribute("u");
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
        }

        try {
            // Devuelve una lista con los usuarios que sean coach de algun equipo
            coachs = entityManager.createNamedQuery("AllCoachs", User.class).getResultList();

        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }

         // Comprueba si alguno de esos coachs tienen el mismo id de usuario que el usuario de la sesion
        for (User u : coachs) {
            if (u.getId() == user.getId()) {
                return true;
            }
        }

        return false;
    }

    

    // ------------------- Logica de creacion de partidos ----------------------

    private void createMatches(Tournament tournament, HttpSession session) {
        // crear partidos
        List<Team> teams = new ArrayList<>();
        TypedQuery<Team> query = entityManager.createQuery(
                "SELECT e.team FROM TournamentTeam e WHERE e.tournament.id = :tournamentid",
                Team.class);

                
        User u = (User) session.getAttribute("u");
        String currentTopics = (String) session.getAttribute("topics");

        teams = query.setParameter("tournamentid", tournament.getId()).getResultList();
        try {
            // Hacerlo random en el futuro, en lugar de por orden de union
            int matchNumber = 1;
            for (int i = 0; i < teams.size(); i += 2) {
                Match match = new Match();

                match.setRoundNumber(1);
                match.setMatchNumber(matchNumber);

                match.setTeam1(teams.get(i));
                match.setTeam2(teams.get(i + 1));

                MessageTopic mt = new MessageTopic();
                mt.setTopicId(UserController.generateRandomBase64Token(6));
                match.setMessageTopic(mt);

                match.setTournament(tournament);

                matchNumber++;

                if(u.hasRole(User.Role.ADMIN) || isUserInMatch(match, u)) {
                    currentTopics = currentTopics + "," + mt.getTopicId();
                    session.setAttribute("topics", currentTopics);
                }

                entityManager.persist(mt);
                entityManager.persist(match);
            }
        } catch (Exception e) {
            log.info("EXXCEPCION: ", e);
        }

        entityManager.flush();
    }

    private boolean isUserInMatch(Match m, User user){
        try{
            List<TeamMember> tm = entityManager.createQuery("select t from TeamMember t where (t.team.id = :team1Id or t.team.id = :team2Id) and t.user.id = :userId",TeamMember.class)
            .setParameter("team1Id", m.getTeam1().getId())
            .setParameter("team2Id", m.getTeam2().getId())
            .setParameter("userId", user.getId())
            .getResultList();
            if(tm.isEmpty())
                return false;
            return true;
        } catch(Exception e){
            return false;
        }
    }

    private List<Tournament> getAllUserTournaments(User u) {
		if (u.getTeam() == null) {
			return new ArrayList<>();
		}
		List<Tournament> query = entityManager.createQuery(
				"SELECT e.tournament FROM TournamentTeam e WHERE e.team.id = :teamId",
				Tournament.class).setParameter("teamId", u.getTeam().getId()).getResultList();

		for (Tournament m : query) {
			log.info("My team is {}, and one of my tournaments is {}", u.getTeam().getId(), m.getId());
		}
		return query;
	}

	private List<String> getAllTopicIds(List<Tournament> tournaments, List<Match> matches) {
		List<String> topicsId = new ArrayList<>();
		for (Tournament tournament : tournaments) {
			if (tournament.getMessageTopic() != null) {
				log.info("my topicid tournament", tournament.getMessageTopic().getTopicId());
				topicsId.add(tournament.getMessageTopic().getTopicId());
			}
		}
		for (Match match : matches) {
			if (match.getMessageTopic().getTopicId() != null) {
				topicsId.add(match.getMessageTopic().getTopicId());
			}
		}
		return topicsId;
	}
}
