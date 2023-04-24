package es.ucm.fdi.iw.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.engine.transaction.spi.JoinStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.io.*;
import java.time.LocalDate;

import es.ucm.fdi.iw.model.Team;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Tournament.TournamentStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import es.ucm.fdi.iw.model.Tournament;
import es.ucm.fdi.iw.model.Match;
import java.util.List;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

/**
 * Non-authenticated requests only.
 */
@Controller
public class RootController {

    private static final Logger log = LogManager.getLogger(RootController.class);
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private void disableViews(Model model) {
        model.addAttribute("home", Boolean.FALSE);
        model.addAttribute("create", Boolean.FALSE);
        model.addAttribute("join", Boolean.FALSE);
        model.addAttribute("onGoing", Boolean.FALSE);
        model.addAttribute("record", Boolean.FALSE);
    }

    @GetMapping("/login")
    public String login(Model model) {
        disableViews(model);
        return "login";
    }

    @GetMapping("/")
    public String index(Model model) {
        disableViews(model);
        model.addAttribute("home", Boolean.TRUE);
        return "index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        disableViews(model);
        model.addAttribute("create", Boolean.TRUE);
        return "create";
    }

    @GetMapping("/join")
    @Transactional
    public String join(Model model, HttpSession session) {
        disableViews(model);
        model.addAttribute("join", Boolean.TRUE);

        User u = entityManager.find(User.class, ((User) session.getAttribute("u")).getId());

        List<Tournament> results = new ArrayList<>();
        Long nTeams = 0L;
        log.info("ANTES DEL MAPA");
        //Map<Tournament, String> mapa = new HashMap<>();

        Map<Tournament, Pair<Long, Integer>> mapa = new HashMap<>();

        // TypedQuery<Tournament> query = entityManager.createQuery(
        //     "SELECT t FROM Tournament t WHERE t.status = :notStartedStatus", Tournament.class);
        // query.setParameter("notStartedStatus", TournamentStatus.NOT_STARTED);
        // List<Tournament> tournaments = query.getResultList();

        results = entityManager.createQuery("select t from Tournament t where t.status = :notStartedStatus", Tournament.class)
        .setParameter("notStartedStatus", TournamentStatus.NOT_STARTED)
        .getResultList();
        for (Tournament tournament : results) {
            long tid = tournament.getId();
            try {
                if (LocalDate.now().isAfter(LocalDate.parse(tournament.getDate())) && tournament.getStatus() == TournamentStatus.NOT_STARTED) {
                    tournament.setStatus(TournamentStatus.ON_GOING);
                    
                    createMatches(tournament);
                }

            } catch (Exception e) {
                model.addAttribute("exception", e.getMessage());
            }
            try {
                TypedQuery<Long> query = entityManager.createQuery(
                        "SELECT count(e.team) FROM Tournament_Team e WHERE e.tournament.id = :tournamentid",
                        Long.class);

                nTeams = query.setParameter("tournamentid", tid).getSingleResult();

            } catch (Exception e) {
                nTeams = 0L;

            }

            String auxTeams = new String(nTeams + "/" + tournament.getMaxTeams());
            Pair<Long, Integer> numT = Pair.of(nTeams, tournament.getMaxTeams());
            log.info("VALOR MAPA" + numT.getFirst() + numT.getSecond());
            mapa.put(tournament, numT);
        }
        model.addAttribute("tournaments", mapa);
        
        return "join";
    }

    private void createMatches(Tournament tournament) {
        //crear partidos
        List<Team> teams = new ArrayList<>();
        TypedQuery<Team> query = entityManager.createQuery(
            "SELECT e.team FROM Tournament_Team e WHERE e.tournament.id = :tournamentid",
            Team.class);

        teams = query.setParameter("tournamentid", tournament.getId()).getResultList();

        //Hacerlo random en el futuro
        //si es potencia de dos
        if((teams.size() & (teams.size() - 1)) == 0) {
            Integer nMatches = teams.size() / 2;
            Integer matchNumber = 1;
            for(Integer i = 0; i < teams.size(); i+=2){
                Match match = new Match();
                match.setRoundNumber(1);
                match.setMatchNumber(matchNumber);
            
                match.setTeam1(teams.get(i));
                match.setTeam2(teams.get(i+1));
            
                match.setTopicId(UserController.generateRandomBase64Token(6));
            
                match.setTournament(tournament);
            
                entityManager.persist(match);
                entityManager.flush();
            
                matchNumber++;
            }                                     
 }
 else {
     //Aqui no se pueden hacer todos los matches (distinto roundnumber)
 }
    }


    @GetMapping("/ongoing")
    public String ongoing(Model model) {
        disableViews(model);
        model.addAttribute("onGoing", Boolean.TRUE);
        return "ongoing";
    }

    @GetMapping("/record")
    public String record(Model model) {
        disableViews(model);
        model.addAttribute("record", Boolean.TRUE);
        return "record";
    }

    /*
     * @GetMapping("/bracket")
     * public String bracket(Model model) {
     * disableViews(model);
     * model.addAttribute("onGoing", Boolean.TRUE);
     * return "bracket";
     * }
     */

    @GetMapping("/register")
    public String register(Model model) {
        disableViews(model);
        return "register";
    }

    /**
     * Registrar usuario
     */
    @PostMapping("/registUser")
    @Transactional
    public RedirectView registUser(@ModelAttribute User registered,
            Model model) throws IOException {
        registered.setEnabled(true);
        registered.setRoles("USER");
        registered.setPassword(encodePassword(registered.getPassword()));
        entityManager.persist(registered);
        entityManager.flush();
        return new RedirectView("/login");
    }

    /**
     * Crear torneo
     */
    @PostMapping("/createTournament")
    @Transactional
    public RedirectView createTournament(@ModelAttribute Tournament tournament,
            Model model) throws Exception {
        tournament.setStatus(TournamentStatus.NOT_STARTED);
        tournament.setCreationDate(LocalDate.now().toString());

        tournament.setRounds(((int) Math.ceil(Math.log(tournament.getMaxTeams()) / Math.log(2))) + 1);

        entityManager.persist(tournament);
        entityManager.flush();
        return new RedirectView("/join");
    }
}
