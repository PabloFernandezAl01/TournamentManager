package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.Message;

import es.ucm.fdi.iw.model.Transferable;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.TeamMember.RoleInTeam;
import es.ucm.fdi.iw.model.User.Role;

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

import javax.persistence.EntityManager;
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

import org.springframework.web.servlet.view.RedirectView;
import java.util.HashMap;
import java.util.Map;
import es.ucm.fdi.iw.model.Team;
import es.ucm.fdi.iw.model.TeamMember;
import es.ucm.fdi.iw.model.Tournament;
import es.ucm.fdi.iw.model.Match;
import es.ucm.fdi.iw.model.Tournament_Team;


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

    /**
     * Landing page for a user profile
     */
    // th:href="@{/tournament/${tournament.key.id}/${session.u.id}}"
    @GetMapping("{tournamentId}/{userId}")
    @Transactional
    public RedirectView index(@PathVariable long tournamentId, @PathVariable long userId, Model model) {
        User targetUser = entityManager.find(User.class, userId);
        Tournament targetTournament = entityManager.find(Tournament.class, tournamentId);
        // model.addAttribute("user", target);
        Team coachingTeam = new Team();
        // coachingTeam.setName("No team registered");
        try {
            coachingTeam = (Team) entityManager.createQuery(
                    "select t from Team t where t.coach.id = :id and not exists (Select tt.team.id from Tournament_Team tt where tt.team.id = t.id)")
                    .setParameter("id", userId).getSingleResult();
            Tournament_Team torunamentTeam = new Tournament_Team();
            torunamentTeam.setTeam(coachingTeam);
            torunamentTeam.setTournament(targetTournament);
            entityManager.persist(torunamentTeam);

        } catch (Exception e) {
        }
        return new RedirectView("/join");

    }

    @GetMapping("{tournamentId}/{userId}/bracket")
    @Transactional
    public String bracket(@PathVariable long tournamentId, @PathVariable long userId, Model model) {
        model.addAttribute("ongoing", Boolean.TRUE);
        

        List<Team> teams = new ArrayList<>();
        Tournament tournament = new Tournament();
        String exception = "HOLA";
        List<Match> matches = new ArrayList<>();
        try{
            teams = entityManager.createQuery(
                "SELECT e.team FROM Tournament_Team e WHERE e.tournament.id = :tournamentid",Team.class).setParameter("tournamentid", tournamentId)
                .getResultList();
            tournament = (Tournament)entityManager.createQuery(
                "SELECT t FROM Tournament t WHERE t.id = :tournamentid",Tournament.class).setParameter("tournamentid", tournamentId)
                .getSingleResult();
            matches = entityManager.createQuery(
                "SELECT m FROM Match m WHERE m.tournament.id = :tournamentid",Match.class).setParameter("tournamentid", tournamentId)
                .getResultList();

        } catch(Exception e){
            exception = e.getMessage();
        }

        Map<Integer, Boolean> partidosRonda = new HashMap<>();

        for(Match match: matches){
            partidosRonda.put(match.getRoundNumber(), true);
        }

        List<Integer> partidosRondaJugables = new ArrayList<>();
        int playerLastRound = tournament.getMaxTeams()/2;

        for(int i = 1; i <= tournament.getRounds(); ++i){

            partidosRondaJugables.add(playerLastRound);
            playerLastRound /= 2;
        }

        model.addAttribute("exception", exception);
        model.addAttribute("teams", teams);
        model.addAttribute("numTeams", teams.size());
        model.addAttribute("tournament", tournament);
        model.addAttribute("matches", matches);
        model.addAttribute("partidosRonda", partidosRonda);
        model.addAttribute("partidosRondaJugables", partidosRondaJugables);
        model.addAttribute("partidos", partidosRondaJugables.get(1));
        return "bracket";
    }
}
