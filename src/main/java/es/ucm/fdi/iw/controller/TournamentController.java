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

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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

    /*
     * 
     */
    @GetMapping("{tournamentId}/{userId}")
    @Transactional
    public RedirectView index(@PathVariable long tournamentId, @PathVariable long userId, Model model) {
        User targetUser = entityManager.find(User.class, userId);
        Tournament targetTournament = entityManager.find(Tournament.class, tournamentId);

        // model.addAttribute("user", target);
        Team coachingTeam = new Team();
        // coachingTeam.setName("No team registered");
        List<Long> team_ids = new ArrayList<>();
        log.info("ANTES DEL TRY");
        try {
            coachingTeam = (Team) entityManager.createQuery(
                    "select t from Team t where t.coach.id = :id ") // and not exists (Select tt.team.id from
                                                                    // Tournament_Team tt where tt.team.id = t.id)
                    .setParameter("id", userId).getSingleResult();

            team_ids = entityManager
                    .createQuery("SELECT t.team FROM Tournament_Team t WHERE t.tournament.id = :tournamentId")
                    .setParameter("tournamentId", tournamentId).getResultList();
            boolean existe = false;
            long id = 0;
            for (Long team_id : team_ids) {
                if ((long) team_id == coachingTeam.getId()) {
                    existe = true;
                    id = team_id;
                }
            }
            if (!existe) {
                Tournament_Team torunamentTeam = new Tournament_Team();
                torunamentTeam.setTeam(coachingTeam);
                torunamentTeam.setTournament(targetTournament);
                entityManager.persist(torunamentTeam);
            }

        } catch (Exception e) {
            model.addAttribute("exception", e.getMessage());
            log.info("Viendo tournament", e);
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
        try {
            teams = entityManager.createQuery(
                    "SELECT e.team FROM Tournament_Team e WHERE e.tournament.id = :tournamentid", Team.class)
                    .setParameter("tournamentid", tournamentId)
                    .getResultList();
            tournament = (Tournament) entityManager.createQuery(
                    "SELECT t FROM Tournament t WHERE t.id = :tournamentid", Tournament.class)
                    .setParameter("tournamentid", tournamentId)
                    .getSingleResult();
            matches = entityManager.createQuery(
                    "SELECT m FROM Match m WHERE m.tournament.id = :tournamentid", Match.class)
                    .setParameter("tournamentid", tournamentId)
                    .getResultList();

        } catch (Exception e) {
            log.warn("Error creating tournament", e);
            throw e;
        }

        Map<Integer, Boolean> partidosRonda = new HashMap<>();

        for (Match match : matches) {
            partidosRonda.put(match.getRoundNumber(), true);
        }

        List<Integer> partidosRondaJugables = new ArrayList<>();
        int playerLastRound = tournament.getMaxTeams() / 2;

        for (int i = 1; i <= tournament.getRounds(); ++i) {

            partidosRondaJugables.add(playerLastRound);
            playerLastRound /= 2;
        }

        // lista aquí con todos los topicId de los partidos en los que está el equipo
        // del usuario

        // COGER EL PARTIDO DE ESTE TORNEO, EN EL QUE ESTA EL EQUIPO DEL USUARIO PARA
        // CARGAR LOS MENSAJES
        User u = entityManager.find(User.class, userId);
        Match matchUserTournament = getUserMatchFromTournament(u, tournament);
        // COGER EL PARTIDO DE ESTE TORNEO, EN EL QUE ESTA EL EQUIPO DEL USUARIO PARA
        // CARGAR LOS MENSAJES

        model.addAttribute("exception", exception);
        model.addAttribute("teams", teams);
        model.addAttribute("numTeams", teams.size());
        model.addAttribute("tournament", tournament);
        model.addAttribute("matches", matches);
        model.addAttribute("partidosRonda", partidosRonda);
        model.addAttribute("partidosRondaJugables", partidosRondaJugables);
        model.addAttribute("partidos", partidosRondaJugables.get(1));

        // AÑADIR EL MATCH DEL USUARIO AL MODELO PARA SABER EL MATCH AL QUE MANDAR MENSAJES
        model.addAttribute("userMatch", matchUserTournament);

        return "bracket";
    }

    private Match getUserMatchFromTournament(User u, Tournament tournament) {

        if (u.getTeam() == null) {
            return null;
        }
        try {
            Match match = entityManager.createQuery(
                    "SELECT e FROM Match e WHERE e.team1.id = :teamId OR e.team2.id = :teamId and e.tournament.id = :tournamentId",
                    Match.class)
                    .setParameter("teamId", u.getTeam().getId())
                    .setParameter("tournamentId", tournament.getId())
                    .getSingleResult();

            log.info("My Match is", match.getMatchNumber());
            return match;

        } catch (NoResultException e) {
            // Manejar la excepción aquí, por ejemplo:
            log.info("No se encontró Match para este usuario en este torneo");
            return null;
        }
    }

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
