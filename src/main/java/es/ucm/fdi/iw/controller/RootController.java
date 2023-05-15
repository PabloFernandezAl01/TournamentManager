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

    @Data
    @AllArgsConstructor
    public static class TourneyData {
        int nTeams;
        int maxTeams;
        String topicId;
        TournamentStatus status;
        boolean isMyTeamJoined;
    }

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
    public String create(Model model, String exception) {
        
        disableViews(model);
        model.addAttribute("create", Boolean.TRUE);
        return "create";
    }

    @GetMapping("/join")
    @Transactional
    public String join(Model model, HttpSession session) {
        disableViews(model);
        model.addAttribute("join", Boolean.TRUE);

        Map<Tournament, TourneyData> tournaments = getModelTournaments(model, session);

        tournaments = getNotStartedTournaments(tournaments);

        model.addAttribute("isUserCoach", isUserCoach(session));
        model.addAttribute("tournaments", tournaments);

        return "join";
    }

    @GetMapping("/ongoing")
    @Transactional
    public String ongoing(Model model, HttpSession session) {
        disableViews(model);
        model.addAttribute("onGoing", Boolean.TRUE);

        Map<Tournament, TourneyData> tournaments = getModelTournaments(model, session);

        tournaments = getOngoingTournaments(tournaments);

        model.addAttribute("isUserCoach", isUserCoach(session));
        model.addAttribute("tournaments", tournaments);

        return "ongoing";
    }

    @GetMapping("/record")
    public String record(Model model, HttpSession session) {
        
        disableViews(model);
        model.addAttribute("record", Boolean.TRUE);

        Map<Tournament, TourneyData> tournaments = getModelTournaments(model, session);

        Map<Tournament, TourneyData> finishedTournaments = getFinishedTournaments(tournaments);
        Map<Tournament, TourneyData> canceledTournaments = getCanceledTournaments(tournaments);

        model.addAttribute("finishedTournaments", finishedTournaments);
        model.addAttribute("canceledTournaments", canceledTournaments);

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

    private Map<Tournament, TourneyData> getModelTournaments(Model model, HttpSession session) {
        Map<Tournament, TourneyData> mapa = new HashMap<>();

        Long nTeams = 0L;

        List<Tournament> tournaments = entityManager
                .createQuery("select t from Tournament t", Tournament.class)
                .getResultList();

        for (Tournament tournament : tournaments) {

            long tournamentId = tournament.getId();
            try {
                TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT count(e.team) FROM Tournament_Team e WHERE e.tournament.id = :tournamentid",
                    Long.class);

                nTeams = query.setParameter("tournamentid", tournamentId).getSingleResult();

                if (LocalDate.now().isAfter(LocalDate.parse(tournament.getDate()))
                 && (tournament.getStatus() == TournamentStatus.NOT_STARTED)) {
                    if(nTeams < tournament.getMaxTeams()){
                        tournament.setStatus(TournamentStatus.CANCELED);
                    }
                    else {
                        tournament.setStatus(TournamentStatus.ON_GOING);

                        //TODO: HACER ESTO DE MANERA ASINCRONA CON WEBSOCKETS
                        createMatches(tournament, session);
                    }
                }

            } catch (Exception e) {
                model.addAttribute("exception", e.getMessage());
                nTeams = 0L;
            }

            TourneyData tourneyData = new TourneyData(nTeams.intValue(), tournament.getMaxTeams(), tournament.getTopicId(), tournament.getStatus(), isMyTeamInTournament(session, tournament));
            mapa.put(tournament, tourneyData);

            log.info("VALOR MAPA" + tourneyData.getNTeams() + tourneyData.getMaxTeams());
        }
        
        return mapa;
    }

    private boolean isMyTeamInTournament(HttpSession session, Tournament tournament)
    {
        User user = (User) session.getAttribute("u");
        List<Team> teams = new ArrayList<>();
        try {
            teams = entityManager.createQuery(
                "SELECT e.team FROM Tournament_Team e WHERE e.tournament.id = :tournamentid", Team.class)
                .setParameter("tournamentid", tournament.getId())
                .getResultList();

            for(Team team : teams) {
                if(team.getCoach().getId() == user.getId()){
                    return true;
                }
            }

            return false;
        }
        catch(Exception e){
            //Si el torneo aun no tiene equipos es obvio que el tuyo no está inscrito
            return false;
        }
    }

    private Map<Tournament, TourneyData> getNotStartedTournaments(Map<Tournament, TourneyData> mapa) {
        Map<Tournament, TourneyData> notStarted = new HashMap<>();

        for (Map.Entry<Tournament, TourneyData> entry : mapa.entrySet()) {

            TourneyData tourneyData = entry.getValue();
            if (tourneyData.getStatus() == TournamentStatus.NOT_STARTED) {
                notStarted.put(entry.getKey(), tourneyData);
            }
        }
        return notStarted;
    }

    private Map<Tournament, TourneyData> getOngoingTournaments(Map<Tournament, TourneyData> mapa) {
        Map<Tournament, TourneyData> ongoing = new HashMap<>();

        for (Map.Entry<Tournament, TourneyData> entry : mapa.entrySet()) {
            TourneyData tourneyData = entry.getValue();

            if (tourneyData.getStatus() == TournamentStatus.ON_GOING) {
                ongoing.put(entry.getKey(), tourneyData);
            }
        }
        return ongoing;
    }

    private Map<Tournament, TourneyData> getFinishedTournaments(Map<Tournament, TourneyData> mapa) {
        Map<Tournament, TourneyData> finished = new HashMap<>();

        for (Map.Entry<Tournament, TourneyData> entry : mapa.entrySet()) {
            TourneyData tourneyData = entry.getValue();
            if (tourneyData.getStatus() == TournamentStatus.FINISHED) {
                finished.put(entry.getKey(), tourneyData);
            }
        }
        return finished;
    }

    private Map<Tournament, TourneyData> getCanceledTournaments(Map<Tournament, TourneyData> mapa) {
        Map<Tournament, TourneyData> canceled = new HashMap<>();

        for (Map.Entry<Tournament, TourneyData> entry : mapa.entrySet()) {
            TourneyData tourneyData = entry.getValue();
            if (tourneyData.getStatus() == TournamentStatus.CANCELED) {
                canceled.put(entry.getKey(), tourneyData);
            }
        }
        return canceled;
    }

    private void createMatches(Tournament tournament, HttpSession session) {
        // crear partidos
        List<Team> teams = new ArrayList<>();
        TypedQuery<Team> query = entityManager.createQuery(
                "SELECT e.team FROM Tournament_Team e WHERE e.tournament.id = :tournamentid",
                Team.class);

        teams = query.setParameter("tournamentid", tournament.getId()).getResultList();

        // Hacerlo random en el futuro, en lugar de por orden de union
        int matchNumber = 1;
        for (int i = 0; i < teams.size(); i += 2) {
            Match match = new Match();

            match.setRoundNumber(1);
            match.setMatchNumber(matchNumber);

            match.setTeam1(teams.get(i));
            match.setTeam2(teams.get(i + 1));

            match.setTopicId(UserController.generateRandomBase64Token(6));
            
            match.setTournament(tournament);

            matchNumber++;

            entityManager.persist(match);
        }
    
        entityManager.flush();
    }

    private boolean isUserCoach(HttpSession session) {
        User user = (User) session.getAttribute("u");
        try {
            entityManager.createQuery(
            "select t from Team t where t.coach.id = :id ") // and not exists (Select tt.team.id from
                                                            // Tournament_Team tt where tt.team.id = t.id)
            .setParameter("id", user.getId()).getSingleResult();

            return true;
        }
        catch(Exception e) {
            return false;
        }
    }
}
