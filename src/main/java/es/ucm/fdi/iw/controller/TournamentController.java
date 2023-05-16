package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.IwUserDetailsService;
import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.Message;

import es.ucm.fdi.iw.model.Transferable;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.TeamMember.RoleInTeam;
import es.ucm.fdi.iw.model.User.Role;
import lombok.extern.java.Log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.criteria.internal.expression.function.AggregationFunction.MAX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.management.Query;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.ArrayList;

import java.io.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

import es.ucm.fdi.iw.model.Tournament.TournamentStatus;
import es.ucm.fdi.iw.model.Tournament.TournamentType;

import org.springframework.web.servlet.view.RedirectView;
import java.util.HashMap;
import java.util.Map;
import es.ucm.fdi.iw.model.Team;
import es.ucm.fdi.iw.model.TeamMember;
import es.ucm.fdi.iw.model.Tournament;
import es.ucm.fdi.iw.model.Match;
import es.ucm.fdi.iw.model.Tournament_Team;
import es.ucm.fdi.iw.model.Tournament.TournamentType;
import java.time.LocalDate;

@Controller()
@RequestMapping("tournament")

public class TournamentController {

    private static final Logger log = LogManager.getLogger(TournamentController.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LocalData localData;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/joinTournament")
    @Transactional
    public RedirectView joinTournament(@RequestParam("tournamentId") long tournamentId,
            @RequestParam("userId") long userId) {
        Tournament targetTournament = entityManager.find(Tournament.class, tournamentId);

        // Equipo del que somos entrenador
        Team coachingTeam = (Team) entityManager.createQuery(
                "select t from Team t where t.coach.id = :id ")
                .setParameter("id", userId).getSingleResult();

        Tournament_Team torunamentTeam = new Tournament_Team();
        torunamentTeam.setTeam(coachingTeam);
        torunamentTeam.setTournament(targetTournament);
        entityManager.persist(torunamentTeam);

        return new RedirectView("/join");
    }

    @GetMapping("{tournamentId}/bracket")
    @Transactional
    public String bracket(HttpSession session, @PathVariable long tournamentId, Model model) {
        model.addAttribute("ongoing", Boolean.TRUE);

        int isLastRound = -1;

        try {
            User user = (User) session.getAttribute("u");

            Tournament tournament = (Tournament) entityManager.createQuery(
                    "SELECT t FROM Tournament t WHERE t.id = :tournamentid", Tournament.class)
                    .setParameter("tournamentid", tournamentId)
                    .getSingleResult();

            int maxRound = 0;
            Match lastMatch = null;

            model.addAttribute("tournament", tournament);

            List<Match> matches = entityManager.createQuery(
                    "SELECT m FROM Match m WHERE m.tournament.id = :tournamentid", Match.class)
                    .setParameter("tournamentid", tournamentId)
                    .getResultList();

            Map<Integer, List<Match>> partidosPorRonda = new HashMap<>();

            List<Match> partidosEnRonda = new ArrayList<>();

            for (Match match : matches) {

                int ronda = match.getRoundNumber();

                log.info("ronda: " + ronda);
                if (maxRound < ronda)
                    maxRound = ronda;

                lastMatch = match;

                partidosEnRonda = partidosPorRonda.getOrDefault(ronda, new ArrayList<>());
                partidosEnRonda.add(match);

                log.info("Ronda : " + ronda + " Partido entre: " + match.getTeam1().getId() + " - "
                        + match.getTeam2().getId());

                partidosPorRonda.put(ronda, partidosEnRonda);

                log.info("partidosPorRonda: " + partidosPorRonda);
            }

            log.info("MaxRound: " + maxRound);

            if (tournament.getType() == 0) {
                if (maxRound == tournament.getRounds() - 1) {
                    if (lastMatch.getWinner() != null) {
                        isLastRound = maxRound;
                        // tournament.setWinner(lastMatch.getWinner());
                        tournament.setStatus(TournamentStatus.FINISHED);
                    }
                } else {
                    boolean allWinners = true;
                    List<Team> winners = new ArrayList<>();
                    for (Match partido : partidosEnRonda) {
                        if (partido.getWinner() == null)
                            allWinners = false;
                        else
                            winners.add(partido.getWinner());
                    }
                    if (allWinners) {
                        log.info("crear partidos de " + maxRound + 1);
                        createMatches(tournament, maxRound + 1, winners);

                        matches = entityManager.createQuery(
                                "SELECT m FROM Match m WHERE m.tournament.id = :tournamentid", Match.class)
                                .setParameter("tournamentid", tournamentId)
                                .getResultList();

                        partidosPorRonda = new HashMap<>();

                        partidosEnRonda = new ArrayList<>();

                        for (Match match : matches) {

                            int ronda = match.getRoundNumber();
                            partidosEnRonda = partidosPorRonda.getOrDefault(ronda, new ArrayList<>());
                            partidosEnRonda.add(match);

                            partidosPorRonda.put(ronda, partidosEnRonda);
                        }
                    }
                }
            } else {

                if (maxRound == tournament.getRounds() - 1) {

                    // tournament.setWinner(lastMatch.getWinner());
                    tournament.setStatus(TournamentStatus.FINISHED);

                } else {
                    boolean allResults = true;
                    List<Team> winners = new ArrayList<>();
                    for (Match partido : partidosEnRonda) {

                        Tournament_Team tournament_Team1 = entityManager.createQuery(
                                "SELECT t FROM Tournament_Team t where t.team.id = :teamid and t.tournament.id = :tournamentid ",
                                Tournament_Team.class)
                                .setParameter("tournamentid", tournamentId)
                                .setParameter("teamid", partido.getTeam1().getId())
                                .getSingleResult();

                        Tournament_Team tournament_Team2 = entityManager.createQuery(
                                "SELECT t FROM Tournament_Team t where t.team.id = :teamid and t.tournament.id = :tournamentid ",
                                Tournament_Team.class)
                                .setParameter("tournamentid", tournamentId)
                                .setParameter("teamid", partido.getTeam2().getId())
                                .getSingleResult();

                        if (partido.getResult() == null) {
                            // 2 - 1 / 1 - 3 , int - int
                            if (partido.getResult().contains("/")) {
                                String[] results = partido.getResult().split("/");
                                String team1 = results[0].trim();
                                String team2 = results[1].trim();
                                if (team1.equals(team2)) {

                                    partido.setResult(team2);
                                    String[] finalResult = team1.split("-");
                                    int number1 = Integer.parseInt(finalResult[0].trim());
                                    int number2 = Integer.parseInt(finalResult[1].trim());

                                    if (number1 > number2) {

                                        partido.setWinner(partido.getTeam1());

                                        tournament_Team1.setVictorias(tournament_Team1.getVictorias()+1);
                                        tournament_Team1.setPuntuacion(tournament_Team1.getPuntuacion()+3);

                                        tournament_Team2.setDerrotas(tournament_Team2.getDerrotas()+1);

                                    } else if (number1 < number2) {

                                        partido.setWinner(partido.getTeam2());

                                        tournament_Team2.setVictorias(tournament_Team2.getVictorias()+1);
                                        tournament_Team2.setPuntuacion(tournament_Team2.getPuntuacion()+3);

                                        tournament_Team1.setDerrotas(tournament_Team1.getDerrotas()+1);

                                    } else {

                                        tournament_Team1.setEmpates(tournament_Team1.getEmpates()+1);
                                        tournament_Team1.setPuntuacion(tournament_Team1.getPuntuacion()+1);

                                        
                                        tournament_Team2.setEmpates(tournament_Team2.getEmpates()+1);
                                        tournament_Team2.setPuntuacion(tournament_Team2.getPuntuacion()+1);
                                    }
                                } else {
                                    allResults = false;
                                }
                            }
                        } else
                            winners.add(partido.getWinner());

                    }
                    if (allWinners) {
                        createMatches(tournament, maxRound + 1, winners);

                        matches = entityManager.createQuery(
                                "SELECT m FROM Match m WHERE m.tournament.id = :tournamentid", Match.class)
                                .setParameter("tournamentid", tournamentId)
                                .getResultList();

                        partidosPorRonda = new HashMap<>();

                        partidosEnRonda = new ArrayList<>();

                        for (Match match : matches) {

                            int ronda = match.getRoundNumber();
                            partidosEnRonda = partidosPorRonda.getOrDefault(ronda, new ArrayList<>());
                            partidosEnRonda.add(match);

                            partidosPorRonda.put(ronda, partidosEnRonda);
                        }
                    }
                }
            }
            boolean isCoach = false;
            if (isUserCoach(session, tournament))
                isCoach = true;

            model.addAttribute("isCoach", isCoach);
            model.addAttribute("partidosPorRonda", partidosPorRonda);
            model.addAttribute("isLastRound", isLastRound);
            model.addAttribute("lastMatch", lastMatch);
            // Match al que mandar mensajes en el chat
            model.addAttribute("userMatch", getUserMatchFromTournament(user, tournament));

            if (tournament.getType() == 0)
                return "bracket";
            else {
                try {
                    List<Tournament_Team> teamsList = entityManager.createQuery(
                            "SELECT t FROM Tournament_Team t WHERE t.tournament.id = :tournamentid ORDER BY t.puntuacion DESC",
                            Tournament_Team.class)
                            .setParameter("tournamentid", tournamentId)
                            .getResultList();
                    model.addAttribute("teams", teamsList);
                } catch (Exception e) {
                    log.info("TOURNAMENT_TEAM EXCEPTION", e);
                }
                return "tableBracket";
            }

        } catch (

        Exception e) {
            log.info("HA SALTADO UNA EXCEPCION: ", e);
        }
        return "bracket";
    }

    @PostMapping("/createTournament")
    @Transactional
    public RedirectView createTournament(@ModelAttribute Tournament tournament,
            Model model) throws Exception {

        if (LocalDateTime.now()
                .isAfter(LocalDateTime.parse(tournament.getDate() + "T" + tournament.getStartingHour() + ":00"))) {
            return new RedirectView(
                    "/create?exception=Incorrect starting date. Date cannot be previous to the current one.");
        }

        tournament.setStatus(TournamentStatus.NOT_STARTED);

        tournament.setCreationDate(LocalDate.now().toString());

        if (tournament.getType() == 1) {
            tournament.setRounds(tournament.getMaxTeams() - 1);
        } else {
            tournament.setRounds(((int) Math.ceil(Math.log(tournament.getMaxTeams()) / Math.log(2))) + 1);
        }

        entityManager.persist(tournament);
        entityManager.flush();

        return new RedirectView("/join");
    }

    private Team getUserTeamFromMatch(User user, Match match) {
        try {
            Team team = entityManager.createQuery(
                    "SELECT m.team FROM TeamMember m WHERE (m.team.id = :matchTeam1 OR m.team.id = :matchTeam2) AND m.user.id = :userId",
                    Team.class)
                    .setParameter("matchTeam1", match.getTeam1().getId())
                    .setParameter("matchTeam2", match.getTeam2().getId())
                    .setParameter("userId", user.getId())
                    .getSingleResult();
            return team;

        } catch (NoResultException e) {
            return null;
        }
    }

    private Match getUserMatchFromTournament(User user, Tournament tournament) {

        if (user.getTeam() == null) {
            return null;
        }

        try {
            Match match = entityManager.createQuery(
                    "SELECT m FROM Match m WHERE (m.team1.id = :teamId OR m.team2.id = :teamId) AND m.tournament.id = :tournamentId AND m.winner IS null",
                    Match.class)
                    .setParameter("teamId", user.getTeam().getId())
                    .setParameter("tournamentId", tournament.getId())
                    .getSingleResult();

            return match;

        } catch (NoResultException e) {
            return null;
        }
    }

    private void createMatches(Tournament tournament, int round, List<Team> winners) {

        // Hacerlo random en el futuro, en lugar de por orden de union
        int matchNumber = 1;
        for (int i = 0; i < winners.size(); i += 2) {
            Match match = new Match();

            match.setRoundNumber(round);
            match.setMatchNumber(matchNumber);

            match.setTeam1(winners.get(i));
            match.setTeam2(winners.get(i + 1));

            match.setTopicId(UserController.generateRandomBase64Token(6));

            match.setTournament(tournament);

            matchNumber++;

            entityManager.persist(match);
        }

        entityManager.flush();
    }

    private boolean isUserCoach(HttpSession session, Tournament tournament) {
        User user = (User) session.getAttribute("u");
        Match match = getUserMatchFromTournament(user, tournament);
        try {
            Team team = (Team) entityManager.createQuery(
                    "select t from Team t where t.coach.id = :id ")
                    .setParameter("id", user.getId()).getSingleResult();

            if (team.getId() == match.getTeam1().getId() || team.getId() == match.getTeam2().getId())
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }
}
