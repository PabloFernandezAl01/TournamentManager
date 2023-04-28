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
import org.springframework.web.servlet.view.RedirectView;
import java.util.HashMap;
import java.util.Map;
import es.ucm.fdi.iw.model.Team;
import es.ucm.fdi.iw.model.TeamMember;
import es.ucm.fdi.iw.model.Tournament;
import es.ucm.fdi.iw.model.Match;
import es.ucm.fdi.iw.model.Tournament_Team;
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
    public RedirectView joinTournament(@RequestParam("tournamentId") long tournamentId, @RequestParam("userId") long userId) {
        Tournament targetTournament = entityManager.find(Tournament.class, tournamentId);

        //Equipo del que somos entrenador
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

        User user = (User) session.getAttribute("u");

        Tournament tournament = (Tournament) entityManager.createQuery(
            "SELECT t FROM Tournament t WHERE t.id = :tournamentid", Tournament.class)
            .setParameter("tournamentid", tournamentId)
            .getSingleResult();

        
        //Match al que mandar mensajes en el chat
        model.addAttribute("userMatch", getUserMatchFromTournament(user, tournament));



        List<Team> teams = entityManager.createQuery(
            "SELECT e.team FROM Tournament_Team e WHERE e.tournament.id = :tournamentid", Team.class)
            .setParameter("tournamentid", tournamentId)
            .getResultList();

        List<Match> matches = entityManager.createQuery(
            "SELECT m FROM Match m WHERE m.tournament.id = :tournamentid", Match.class)
            .setParameter("tournamentid", tournamentId)
            .getResultList();
        




        Map<Integer, Boolean> partidosRonda = new HashMap<>();

        for (Match match : matches) {
            partidosRonda.put(match.getRoundNumber(), true);
        }

        log.warn("lolazo3");

        List<Integer> partidosRondaJugables = new ArrayList<>();
        int playerLastRound = tournament.getMaxTeams() / 2;

        for (int i = 1; i <= tournament.getRounds(); ++i) {

            partidosRondaJugables.add(playerLastRound);
            playerLastRound /= 2;
        }



        model.addAttribute("teams", teams);
        model.addAttribute("numTeams", teams.size());
        model.addAttribute("tournament", tournament);
        model.addAttribute("matches", matches);
        model.addAttribute("partidosRonda", partidosRonda);
        model.addAttribute("partidosRondaJugables", partidosRondaJugables);
        model.addAttribute("partidos", partidosRondaJugables.get(1));


        return "bracket";
    }

    private Match getUserMatchFromTournament(User user, Tournament tournament) {

        if (user.getTeam() == null) {
            return null;
        }

        try {
            Match match = entityManager.createQuery(
                    "SELECT m FROM Match m WHERE m.team1.id = :teamId OR m.team2.id = :teamId AND m.tournament.id = :tournamentId",
                    Match.class)
                    .setParameter("teamId", user.getTeam().getId())
                    .setParameter("tournamentId", tournament.getId())
                    .getSingleResult();


            return match;

        } catch (NoResultException e) {
            return null;
        }
    }

    @PostMapping("/createTournament")
    @Transactional
    public RedirectView createTournament(@ModelAttribute Tournament tournament,
            Model model) throws Exception {

        if(LocalDateTime.now().isAfter(LocalDateTime.parse(tournament.getDate() + "T" + tournament.getStartingHour() + ":00"))) {
            return new RedirectView("/create?exception=Incorrect starting date. Date cannot be previous to the current one.");
        }

        tournament.setStatus(TournamentStatus.NOT_STARTED);

        tournament.setCreationDate(LocalDate.now().toString());

        tournament.setRounds(((int) Math.ceil(Math.log(tournament.getMaxTeams()) / Math.log(2))) + 1);

        entityManager.persist(tournament);
        entityManager.flush();

        return new RedirectView("/join");
    }
}
