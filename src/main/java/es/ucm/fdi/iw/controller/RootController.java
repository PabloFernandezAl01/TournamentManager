package es.ucm.fdi.iw.controller;
import es.ucm.fdi.iw.model.Team;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Tournament.TournamentStatus;
import es.ucm.fdi.iw.model.Tournament;
import es.ucm.fdi.iw.model.Match;

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
import javax.transaction.Transactional;
import javax.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final Logger log = LogManager.getLogger(RootController.class);

    /*
     * Estructura para manejar la informacion de un torneo
     */
    @Data
    @AllArgsConstructor
    public static class Tourney {
        int nTeams;
        int maxTeams;
        TournamentStatus status;
        boolean isMyTeamJoined;
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
        model.addAttribute("home", "active");
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

        // Consulta a la DB para obtener los torneos
        Map<Tournament, Tourney> tournaments = getAllTournaments(model, session);

        // Filtra los torneos que no han empezado
        tournaments = getNotStartedTournaments(tournaments);

        // Añande los torneos y un flag indicando si el usuario es coach
        model.addAttribute("isUserCoach", true);
        model.addAttribute("tournaments", tournaments);

        return "join";
    }

    @GetMapping("/ongoing")
    @Transactional
    public String ongoing(Model model, HttpSession session) {
        model.addAttribute("ongoing", "active");

        // Consulta a la DB para obtener los torneos
        Map<Tournament, Tourney> tournaments = getAllTournaments(model, session);

        // Filtra para obtener los torneos que se estan jugando
        tournaments = getOngoingTournaments(tournaments);

        // Añande los torneos y un flag indicando si el usuario es coach
        model.addAttribute("isUserCoach", true);
        model.addAttribute("tournaments", tournaments);
    
        return "ongoing";
    }

    @GetMapping("/record")
    public String record(Model model, HttpSession session) {
        model.addAttribute("record", "active");

        // Consulta a la DB para obtener los torneos
        Map<Tournament, Tourney> tournaments = getAllTournaments(model, session);

        // Filtra para obtener los torneos que han terminado y los que han sido cancelados
        Map<Tournament, Tourney> finishedTournaments = getFinishedTournaments(tournaments);
        Map<Tournament, Tourney> canceledTournaments = getCanceledTournaments(tournaments);

        // Añande los torneos
        model.addAttribute("finishedTournaments", finishedTournaments);
        model.addAttribute("canceledTournaments", canceledTournaments);

        return "record";
    }


    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("register", "active");
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

    private Map<Tournament, Tourney> getAllTournaments(Model model, HttpSession session) {

        // Mapa que almacena 
        Map<Tournament, Tourney> mapa = new HashMap<>();

        // Consulta a la DB para obtener todos los torneos
        List<Tournament> tournaments = entityManager.createNamedQuery("AllTournaments", Tournament.class).getResultList();

        int nTeams = 0;
        for (Tournament tournament : tournaments) {

            long id = tournament.getId();
            
            try {
                // Consulta para obtener cuantos equipos estan inscritos en un torneo
                nTeams = entityManager.createNamedQuery("TeamsIdsByTournament", Team.class).setParameter("tournamentid", id).getResultList().size();
                
                // Si ha llegado la fecha de comienzo del torneo y esta marcado como "No empezado"
                if (LocalDate.now().isAfter(LocalDate.parse(tournament.getDate())) && (tournament.getStatus() == TournamentStatus.NOT_STARTED)) {

                    // Si no se ha completado el numero de equipos se cancela
                    if(nTeams < tournament.getMaxTeams()) {
                        tournament.setStatus(TournamentStatus.CANCELED);
                    }
                    else { // En caso contrario, se marca como "On_Going" y se crean los encuentros
                        tournament.setStatus(TournamentStatus.ON_GOING);

                        //TODO: Hacer esto asíncrono con WebSockets
                        createMatches(tournament.getId(), session);
                    }
                }

            } catch (Exception e) {
                model.addAttribute("exception", e.getMessage());
                nTeams = 0;
            }

            // Se añade el torneo al mapa
            Tourney tourney = new Tourney(nTeams, tournament.getMaxTeams(), tournament.getStatus(), isMyTeamInTournament(session, tournament.getId()));
            mapa.put(tournament, tourney);

            log.info("Valor del mapa: " + tourney.getNTeams() + tourney.getMaxTeams());
        }
        
        return mapa;
    }

    private boolean isMyTeamInTournament(HttpSession session, long tournamentId) {

        User user = (User) session.getAttribute("u");

        List<Long> teamsIds;
        List<Long> usersIdsInTeam;

        try {
            teamsIds = entityManager.createNamedQuery("TeamsIdsByTournament", Long.class).setParameter("tournamentid", tournamentId).getResultList();

            for (Long teamId : teamsIds) {

                usersIdsInTeam = entityManager.createNamedQuery("MembersIdsByTeam", Long.class).setParameter("teamid", teamId).getResultList();
                
                for (Long u : usersIdsInTeam) {
                    if (u == user.getId()) {
                        return true;
                    }
                }
            }

            return false;
        }
        catch(IllegalArgumentException e){
            System.err.print(e.getMessage());
            return false;
        }
    }

    private Map<Tournament, Tourney> getNotStartedTournaments(Map<Tournament, Tourney> mapa) {
        Map<Tournament, Tourney> notStarted = new HashMap<>();

        for (Map.Entry<Tournament, Tourney> entry : mapa.entrySet()) {
            Tourney tourneyData = entry.getValue();
            if (tourneyData.getStatus() == TournamentStatus.NOT_STARTED) {
                notStarted.put(entry.getKey(), tourneyData);
            }
        }
        return notStarted;
    }

    private Map<Tournament, Tourney> getOngoingTournaments(Map<Tournament, Tourney> mapa) {
        Map<Tournament, Tourney> ongoing = new HashMap<>();

        for (Map.Entry<Tournament, Tourney> entry : mapa.entrySet()) {
            Tourney tourneyData = entry.getValue();
            if (tourneyData.getStatus() == TournamentStatus.ON_GOING) {
                ongoing.put(entry.getKey(), tourneyData);
            }
        }
        return ongoing;
    }

    private Map<Tournament, Tourney> getFinishedTournaments(Map<Tournament, Tourney> mapa) {
        Map<Tournament, Tourney> finished = new HashMap<>();

        for (Map.Entry<Tournament, Tourney> entry : mapa.entrySet()) {
            Tourney tourneyData = entry.getValue();
            if (tourneyData.getStatus() == TournamentStatus.FINISHED) {
                finished.put(entry.getKey(), tourneyData);
            }
        }
        return finished;
    }

    private Map<Tournament, Tourney> getCanceledTournaments(Map<Tournament, Tourney> mapa) {
        Map<Tournament, Tourney> canceled = new HashMap<>();

        for (Map.Entry<Tournament, Tourney> entry : mapa.entrySet()) {
            Tourney tourneyData = entry.getValue();
            if (tourneyData.getStatus() == TournamentStatus.CANCELED) {
                canceled.put(entry.getKey(), tourneyData);
            }
        }
        return canceled;
    }

    private void createMatches(Long tournamentId, HttpSession session) {
        List<Long> teamsId = entityManager.createNamedQuery("TeamsIdsByTournament", Long.class).setParameter("tournamentid", tournamentId).getResultList();

        for (int i = 0; i < teamsId.size(); i += 2) {

            Match match = new Match();

            match.setRound(1);
            match.setTeamOne(i);
            match.setTeamTwo(i);
            match.setTournamentId(tournamentId);

            entityManager.persist(match);
        }
    
        entityManager.flush();
    }

    // private boolean isUserCoach(HttpSession session) {
    //     User user = (User) session.getAttribute("u");
    //     try {
    //         entityManager.createQuery("select t from Team t where t.coach.id = :id ").setParameter("id", user.getId()).getSingleResult();

    //         return true;
    //     }
    //     catch(Exception e) {
    //         return false;
    //     }
    // }
}
