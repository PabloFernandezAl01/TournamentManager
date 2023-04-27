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
import lombok.AllArgsConstructor;
import lombok.Data;

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


    @GetMapping("/")
    public String homepage(Model model) {
        disableViews(model);
        model.addAttribute("home", Boolean.TRUE);
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        disableViews(model);
        return "login";
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
        // Map<Tournament, String> mapa = new HashMap<>();

        Map<Tournament, TourneyData> mapa = new HashMap<>();

        // TypedQuery<Tournament> query = entityManager.createQuery(
        // "SELECT t FROM Tournament t WHERE t.status = :notStartedStatus",
        // Tournament.class);
        // query.setParameter("notStartedStatus", TournamentStatus.NOT_STARTED);
        // List<Tournament> tournaments = query.getResultList();

        //where t.status = :notStartedStatus
        results = entityManager
                .createQuery("select t from Tournament t", Tournament.class)
                //.setParameter("notStartedStatus", TournamentStatus.NOT_STARTED)
                .getResultList();
        for (Tournament tournament : results) {
            long tid = tournament.getId();
            try {
                if (LocalDate.now().isAfter(LocalDate.parse(tournament.getDate()))
                        && tournament.getStatus() == TournamentStatus.NOT_STARTED) {
                    tournament.setStatus(TournamentStatus.ON_GOING);

                    createMatches(tournament, session);
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
            TourneyData td = new TourneyData(nTeams.intValue(), tournament.getMaxTeams(), tournament.getTopicId());
            log.info("VALOR MAPA" + td.getNTeams() + td.getMaxTeams());
            mapa.put(tournament, td);
        }
        model.addAttribute("tournaments", mapa);

        return "join";
    }

    @Data
    @AllArgsConstructor
    public static class TourneyData {
        int nTeams;
        int maxTeams;
        String topicId;
    }

    private void createMatches(Tournament tournament, HttpSession session) {
        // crear partidos
        List<Team> teams = new ArrayList<>();
        TypedQuery<Team> query = entityManager.createQuery(
                "SELECT e.team FROM Tournament_Team e WHERE e.tournament.id = :tournamentid",
                Team.class);

        teams = query.setParameter("tournamentid", tournament.getId()).getResultList();

        // Hacerlo random en el futuro
        // si es potencia de dos
        if ((teams.size() & (teams.size() - 1)) == 0) {
            int nMatches = teams.size() / 2;
            int matchNumber = 1;
            for (int i = 0; i < teams.size(); i += 2) {
                Match match = new Match();
                match.setRoundNumber(1);
                match.setMatchNumber(matchNumber);

                match.setTeam1(teams.get(i));
                match.setTeam2(teams.get(i + 1));

                match.setTopicId(UserController.generateRandomBase64Token(6));
                List<String> topics = new ArrayList<>();
                log.info("topics 1", session.getAttribute("topics"));
                User u = entityManager.find(User.class, ((User) session.getAttribute("u")).getId());
                if (u.getTeam().getId() == match.getTeam1().getId()
                        || u.getTeam().getId() == match.getTeam2().getId()) {
                    if (session.getAttribute("topics") != null) {
                        topics = (ArrayList) session.getAttribute("topics");
                        log.info("topics 1", topics);
                    }
                    topics.add(match.getTopicId());
                    session.setAttribute("topics", topics);
                }

                match.setTournament(tournament);
                entityManager.persist(match);

                matchNumber++;
            }
        } else {
            // Aqui no se pueden hacer todos los matches (distinto roundnumber)
        }
        entityManager.flush();
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


    @GetMapping("/register")
    public String register(Model model) {
        disableViews(model);
        return "register";
    }


    @PostMapping("/register")
    @Transactional
    public RedirectView register(@ModelAttribute User registered,
            Model model) throws IOException {

        registered.setEnabled(true);
        registered.setRoles("USER");
        registered.setPassword(encodePassword(registered.getPassword()));
        registered.setTeam(null);
        entityManager.persist(registered);
        entityManager.flush();
        return new RedirectView("/login");
    }
}
