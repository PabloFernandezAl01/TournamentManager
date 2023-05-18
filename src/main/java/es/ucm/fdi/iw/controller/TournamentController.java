package es.ucm.fdi.iw.controller;
import es.ucm.fdi.iw.model.Tournament.TournamentStatus;
import es.ucm.fdi.iw.model.TournamentTeam;
import es.ucm.fdi.iw.model.Tournament;
import es.ucm.fdi.iw.model.MessageTopic;
import es.ucm.fdi.iw.model.Match;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.stereotype.Controller;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.ResponseEntity;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;

import javax.persistence.NoResultException;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.servlet.http.HttpSession;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller()
@RequestMapping("tournament")

public class TournamentController {

    private static final Logger log = LogManager.getLogger(TournamentController.class);

    @Autowired
    private EntityManager entityManager;

    @PostMapping("/joinTournament")
    @Transactional
    public RedirectView joinTournament(@RequestParam("tournamentId") long tournamentId, @RequestParam("userId") long userId) {

        Tournament tournamentToJoin = entityManager.find(Tournament.class, tournamentId);

        Team coachingTeam = null;

        try {
            // Consulta para obtener el equipo del cual es coach el usuario
            coachingTeam = (Team) entityManager.createQuery(
                "select t from Team t where t.coach.id = :id ")
                .setParameter("id", userId).getSingleResult();
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }

        // Se crea el TournamentTeam (relacion entre equipo y torneo)
        /*
         * - Team: El equipo del usuario que solicito la inscripcion de su equipo en el torneo (coach)
         * - Tournament: Torneo elegido para inscribirse (obtenido como parametro)
         */
        TournamentTeam tournamentTeam = new TournamentTeam();
        tournamentTeam.setTeam(coachingTeam);
        tournamentTeam.setTournament(tournamentToJoin);

        // Se persisten el TournamentTeam
        entityManager.persist(tournamentTeam);

        return new RedirectView("/join");
    }

    @GetMapping("{tournamentId}/bracket")
    @Transactional
    public String bracket(HttpSession session, @PathVariable long tournamentId, Model model) {

        model.addAttribute("ongoing", Boolean.TRUE);

        int isLastRound = -1;

        try {
            User user = (User) session.getAttribute("u");
            boolean isCoach = false;
            boolean isFinalResult = true;
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
            boolean allResults = true;
            // EL TORNEO ES ELIMINACION SIMPLE
            if (tournament.getType() == 0) {
                // SI ES EL ULTIMO PARTIDO
                if (maxRound == tournament.getRounds() - 1) {
                    if (lastMatch.getResult() != null) {

                        if (lastMatch.getResult().contains("/")) {
                            isFinalResult = false;
                            String[] results = lastMatch.getResult().split("/");
                            String team1 = results[0].trim();
                            String team2 = results[1].trim();
                            // SI EL RESULTADO ENVIADO POR AMBOS EQUIPOS ES EL MISMO
                            if (team1.equals(team2)) {
                                lastMatch.setResult(team2);
                                String[] finalResult = team1.split("-");
                                int number1 = Integer.parseInt(finalResult[0].trim());
                                int number2 = Integer.parseInt(finalResult[1].trim());

                                // GANA EQUIPO 1
                                if (number1 > number2) {

                                    lastMatch.setWinner(lastMatch.getTeam1());

                                }
                                // GANA EQUIPO 2
                                else {

                                    lastMatch.setWinner(lastMatch.getTeam2());

                                }
                            } 
                            // LOS EQUIPOS NO ESTAN DE ACUERDO EN EL RESULTADO
                            else {
                                // ENVIAR A ADMIN
                            }
                        }
                    } else{
                        isFinalResult = false;
                    }
                    if (lastMatch.getWinner() != null) {
                        isLastRound = maxRound;
                        // tournament.setWinner(lastMatch.getWinner());
                        tournament.setStatus(TournamentStatus.FINISHED);
                    }
                }
                // SI NO ES EL ULTIMO PARTIDO
                else {
                    for (Match partido : partidosEnRonda) {
                        if (partido.getResult() != null) {
                            // 2 - 1 / 1 - 3 , int - int

                            if (partido.getResult().contains("/")) {
                                isFinalResult = false;
                                String[] results = partido.getResult().split("/");
                                String team1 = results[0].trim();
                                String team2 = results[1].trim();
                                // SI EL RESULTADO ENVIADO POR AMBOS EQUIPOS ES EL MISMO
                                if (team1.equals(team2)) {
                                    partido.setResult(team2);
                                    String[] finalResult = team1.split("-");
                                    int number1 = Integer.parseInt(finalResult[0].trim());
                                    int number2 = Integer.parseInt(finalResult[1].trim());

                                    // GANA EQUIPO 1
                                    if (number1 > number2) {

                                        partido.setWinner(partido.getTeam1());

                                    }
                                    // GANA EQUIPO 2
                                    else {

                                        partido.setWinner(partido.getTeam2());

                                    }
                                }
                                // LOS EQUIPOS NO ESTAN DE ACUERDO EN EL RESULTADO
                                else {
                                    // ENVIAR A ADMIN
                                    allResults = false;
                                }
                            } else {
                                allResults = true;
                            }

                        } else {
                            isFinalResult = false;
                            allResults = false;
                        }
                    }
                    List<Team> winners = new ArrayList<>();
                    boolean allWinners = true;
                    for (Match partido : partidosEnRonda) {
                        if (partido.getWinner() == null)
                            allWinners = false;
                        else
                            winners.add(partido.getWinner());
                    }
                    // SI LA JORNADA ANTERIOR HA ACABADO
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

                            if (maxRound < ronda)
                                maxRound = ronda;

                            partidosEnRonda = partidosPorRonda.getOrDefault(ronda, new ArrayList<>());
                            partidosEnRonda.add(match);

                            partidosPorRonda.put(ronda, partidosEnRonda);
                        }
                    }
                }
            }
            // EL TORNEO ES TIPO LIGA
            else {

                if (isUserCoachLeague(session, tournament, maxRound))
                    isCoach = true;

                model.addAttribute("isCoach", isCoach);
                // PARA CADA PARTIDO EN LA JORnADA
                for (Match partido : partidosEnRonda) {

                    TournamentTeam tournament_Team1 = entityManager.createQuery(
                            "SELECT t FROM TournamentTeam t WHERE t.team.id = :teamid and t.tournament.id = :tournamentid ",
                            TournamentTeam.class)
                            .setParameter("tournamentid", tournament.getId())
                            .setParameter("teamid", partido.getTeam1().getId())
                            .getSingleResult();

                    TournamentTeam tournament_Team2 = entityManager.createQuery(
                            "SELECT t FROM TournamentTeam t WHERE t.team.id = :teamid and t.tournament.id = :tournamentid ",
                            TournamentTeam.class)
                            .setParameter("tournamentid", tournament.getId())
                            .setParameter("teamid", partido.getTeam2().getId())
                            .getSingleResult();

                    // SI HAY RESULTADO DEL PARTIDO
                    if (partido.getResult() != null) {

                        // 2 - 1 / 1 - 3 , int - int

                        if (partido.getResult().contains("/")) {
                            isFinalResult = false;
                            String[] results = partido.getResult().split("/");
                            String team1 = results[0].trim();
                            String team2 = results[1].trim();
                            // SI EL RESULTADO ENVIADO POR AMBOS EQUIPOS ES EL MISMO
                            if (team1.equals(team2)) {

                                partido.setResult(team2);
                                log.info("RESULTADO PARTIDO: " + partido.getResult());
                                String[] finalResult = team1.split("-");
                                int number1 = Integer.parseInt(finalResult[0].trim());
                                int number2 = Integer.parseInt(finalResult[1].trim());

                                // GANA EQUIPO 1
                                if (number1 > number2) {

                                    partido.setWinner(partido.getTeam1());

                                    tournament_Team1.setVictorias(tournament_Team1.getVictorias() + 1);
                                    tournament_Team1.setPuntuacion(tournament_Team1.getPuntuacion() + 3);

                                    tournament_Team2.setDerrotas(tournament_Team2.getDerrotas() + 1);

                                }
                                // GANA EQUIPO 2
                                else if (number1 < number2) {

                                    partido.setWinner(partido.getTeam2());

                                    tournament_Team2.setVictorias(tournament_Team2.getVictorias() + 1);
                                    tournament_Team2.setPuntuacion(tournament_Team2.getPuntuacion() + 3);

                                    tournament_Team1.setDerrotas(tournament_Team1.getDerrotas() + 1);

                                }
                                // EMPATE
                                else {

                                    tournament_Team1.setEmpates(tournament_Team1.getEmpates() + 1);
                                    tournament_Team1.setPuntuacion(tournament_Team1.getPuntuacion() + 1);

                                    tournament_Team2.setEmpates(tournament_Team2.getEmpates() + 1);
                                    tournament_Team2.setPuntuacion(tournament_Team2.getPuntuacion() + 1);
                                }
                            }
                            // LOS EQUIPOS NO ESTAN DE ACUERDO EN EL RESULTADO
                            else {
                                // ENVIAR A ADMIN
                                allResults = false;
                            }
                        } else {
                        }
                    } else {
                        isFinalResult = false;
                        allResults = false;
                    }
                }
                // SI LA JORNADA ANTERIOR HA ACABADO
                int lastRound = maxRound;
                if (allResults) {

                    createMatchesLeague(tournament, maxRound);

                    matches = entityManager.createQuery(
                            "SELECT m FROM Match m WHERE m.tournament.id = :tournamentid", Match.class)
                            .setParameter("tournamentid", tournamentId)
                            .getResultList();

                    partidosPorRonda = new HashMap<>();

                    partidosEnRonda = new ArrayList<>();

                    for (Match match : matches) {

                        int ronda = match.getRoundNumber();
                        if (maxRound < ronda)
                            maxRound = ronda;
                        partidosEnRonda = partidosPorRonda.getOrDefault(ronda, new ArrayList<>());
                        partidosEnRonda.add(match);
                        
                        partidosPorRonda.put(ronda, partidosEnRonda);
                    }
                }
                if (lastRound == tournament.getRounds() && allResults) {
                    // tournament.setWinner(lastMatch.getWinner());
                    tournament.setStatus(TournamentStatus.FINISHED);
                }
            }

            model.addAttribute("partidosPorRonda", partidosPorRonda);
            model.addAttribute("isLastRound", isLastRound);
            model.addAttribute("lastMatch", lastMatch);
            model.addAttribute("isFinalResult", isFinalResult);

            if (tournament.getType() == 0) {
                if (isUserCoach(session, tournament))
                    isCoach = true;
                model.addAttribute("isCoach", isCoach);
                // Match al que mandar mensajes en el chat
                model.addAttribute("userMatch", getUserMatchFromTournament(user, tournament));
                // team del user
                model.addAttribute("userTeam",
                        getUserTeamFromMatch(user, getUserMatchFromTournament(user, tournament)));
                return "bracket";

            } else {
                try {
                    // Match al que mandar mensajes en el chat
                    model.addAttribute("userMatch", getUserMatchFromTournamentLeague(user, tournament, maxRound));
                    // team del usuario
                    model.addAttribute("userTeam",getUserTeamFromMatch(user, getUserMatchFromTournamentLeague(user, tournament, maxRound)));
                    List<TournamentTeam> teamsList = entityManager.createQuery(
                            "SELECT t FROM TournamentTeam t WHERE t.tournament.id = :tournamentid ORDER BY t.puntuacion DESC",
                            TournamentTeam.class)
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

        MessageTopic mt = new MessageTopic();
        mt.setTopicId(UserController.generateRandomBase64Token(6));

        tournament.setMessageTopic(mt);

        tournament.setDate(LocalDate.now().toString());

        if (tournament.getType() == 1) {
            tournament.setRounds(tournament.getMaxTeams() - 1);

        } else {
            tournament.setRounds(((int) Math.ceil(Math.log(tournament.getMaxTeams()) / Math.log(2))) + 1);
        }

        entityManager.persist(mt);
        entityManager.persist(tournament);
        entityManager.flush();

        return new RedirectView("/join");
    }

    private Team getUserTeamFromMatch(User user, Match match) {
        if(match == null)
            return null;
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

    private Match getUserMatchFromTournamentLeague(User user, Tournament tournament, int maxRound) {
        if (user.getTeam() == null) {
            return null;
        }

        try {
            Match match = entityManager.createQuery(
                    "SELECT m FROM Match m WHERE (m.team1.id = :teamId OR m.team2.id = :teamId) AND m.tournament.id = :tournamentId AND m.roundNumber = :maxRound",
                    Match.class)
                    .setParameter("teamId", user.getTeam().getId())
                    .setParameter("tournamentId", tournament.getId())
                    .setParameter("maxRound", maxRound)
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

            MessageTopic mt = new MessageTopic();
            mt.setTopicId(UserController.generateRandomBase64Token(6));
            
            match.setMessageTopic(mt);

            match.setRoundNumber(round);
            match.setMatchNumber(matchNumber);

            match.setTeam1(winners.get(i));
            match.setTeam2(winners.get(i + 1));


            match.setTournament(tournament);

            matchNumber++;

            entityManager.persist(mt);
            entityManager.persist(match);
        }

        entityManager.flush();
    }

    private void createMatchesLeague(Tournament tournament, int currentRound) {
        List<Match> currentRoundMatches = getMatchesByRound(tournament, currentRound);
        List<Team> teams = getTeamsForTournament(tournament);

        // Verificar si ya se han jugado todos los partidos
        if (currentRoundMatches.size() < teams.size() / 2) {
            System.out.println("No se han jugado todos los partidos de la ronda actual.");
            return;
        }

        int nextRound = currentRound + 1;
        int matchNumber = 1;

        // Crear los partidos de la siguiente jornada
        for (int i = 0; i < teams.size() - 1; i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                // Verificar si los equipos ya han jugado antes
                log.info("YA HAY PARTIDO ENTRE ELLOS: " + havePlayedBefore(teams.get(i), teams.get(j), tournament));
                if (havePlayedBefore(teams.get(i), teams.get(j), tournament)) {
                    continue; // Pasar al siguiente par de equipos
                }

                // Verificar si alguno de los equipos ya tiene un partido en la nueva jornada
                if (hasMatchInNextRound(teams.get(i), nextRound, tournament)
                        || hasMatchInNextRound(teams.get(j), nextRound, tournament)) {
                    continue; // Pasar al siguiente par de equipos
                }

                Match match = new Match();
                match.setRoundNumber(nextRound);
                match.setMatchNumber(matchNumber);
                match.setTeam1(teams.get(i));
                match.setTeam2(teams.get(j));
                match.setTournament(tournament);

                entityManager.persist(match);

                matchNumber++;
            }
        }

        entityManager.flush();
    }

    private boolean havePlayedBefore(Team team1, Team team2, Tournament tournament) {

        try {
            entityManager.createQuery(
                    "SELECT m FROM Match m WHERE ((m.team1.id = :team1Id AND m.team2.id = :team2Id) OR (m.team1.id = :team2Id AND m.team2.id = :team1Id)) AND m.tournament.id = :tournamentId",
                    Match.class)
                    .setParameter("team1Id", team1.getId())
                    .setParameter("team2Id", team2.getId())
                    .setParameter("tournamentId", tournament.getId())
                    .getSingleResult();

            return true;

        } catch (NoResultException e) {
            return false;
        }

    }

    private boolean hasMatchInNextRound(Team team, int nextRound, Tournament tournament) {
        try {
            entityManager.createQuery(
                    "SELECT m FROM Match m WHERE (m.team1.id = :team1Id OR m.team2.id = :team1Id) AND  m.tournament.id = :tournamentId  AND m.roundNumber = :nextRound",
                    Match.class)
                    .setParameter("team1Id", team.getId())

                    .setParameter("tournamentId", tournament.getId())
                    .setParameter("nextRound", nextRound)
                    .getSingleResult();

            return true;

        } catch (NoResultException e) {
            return false;
        }
    }

    private List<Team> getTeamsForTournament(Tournament tournament) {
        TypedQuery<Team> query = entityManager.createQuery(
                "SELECT t.team FROM TournamentTeam t WHERE t.tournament.id = :tournamentid",
                Team.class);

        return query.setParameter("tournamentid", tournament.getId())
                .getResultList();
    }

    private List<Match> getMatchesByRound(Tournament tournament, int roundNumber) {
        TypedQuery<Match> query = entityManager.createQuery(
                "SELECT m FROM Match m WHERE m.tournament.id = :tournamentid AND m.roundNumber = :roundNumber",
                Match.class);

        return query.setParameter("tournamentid", tournament.getId())
                .setParameter("roundNumber", roundNumber)
                .getResultList();
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

    private boolean isUserCoachLeague(HttpSession session, Tournament tournament, int maxRound) {
        User user = (User) session.getAttribute("u");
        Match match = getUserMatchFromTournamentLeague(user, tournament, maxRound);
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

    @PostMapping("sendResults/{tournamentId}/{matchId}/{userTeamId}")
    @Transactional
    public ResponseEntity<?> sendResults(@RequestParam("resultadoTeam1") int resultadoTeam1,
            @RequestParam("resultadoTeam2") int resultadoTeam2, HttpSession session, @PathVariable long tournamentId,
            @PathVariable long matchId, @PathVariable long userTeamId, Model model) {
        try {
            Match match = entityManager
                    .createQuery("select m from Match m where m.id = :matchId AND m.tournament.id = :tournamentId ",
                            Match.class)
                    .setParameter("matchId", matchId)
                    .setParameter("tournamentId", tournamentId)
                    .getSingleResult();

            String resultadoNuevo = resultadoTeam1 + " - " + resultadoTeam2;
            // si hay resultado
            if (match.getResult() != null) {
                if (match.getResult().contains("/")) {
                    int posBarra = match.getResult().indexOf("/");

                    // El carácter "/" está al principio, el result del team2 ya esta
                    if (posBarra == 0) {
                        // si el team del usuario es el team1
                        if (userTeamId == match.getTeam1().getId()) {
                            match.setResult(resultadoNuevo + match.getResult());
                        }
                        // si el team del usuario es el team2
                        else {
                            // sobreescribir antiguo resultado enviado
                            match.setResult(" / " + resultadoNuevo);
                        }
                    }
                    // El caracter "/" esta al final, el result del team 1 ya está
                    else if (posBarra == match.getResult().length() - 1) {
                        // si el team del usuario es el team1
                        if (userTeamId == match.getTeam1().getId()) {
                            // sobreescribir antiguo resultado
                            match.setResult(resultadoNuevo + " / ");
                        }
                        // si el team del usuario es el team2
                        else {
                            match.setResult(match.getResult() + resultadoNuevo);
                        }
                    }
                    // Ya hay dos resultados
                    else {
                        String[] results = match.getResult().split("/");
                        String team1 = results[0].trim();
                        String team2 = results[1].trim();

                        // si el team del usuario es el team1
                        if (userTeamId == match.getTeam1().getId()) {
                            // sobreescribir antiguo resultado
                            match.setResult(resultadoNuevo + " / " + team2);
                        }
                        // si el team del usuario es el team2
                        else {
                            match.setResult(team1 + " / " + resultadoNuevo);
                        }
                    }
                } else {
                    boolean isFinalResult = true;
                    model.addAttribute("isFinalResult", isFinalResult);
                }
            }
            // si no hay resultado aún
            else {
                // si el team del usuario es team1
                if (userTeamId == match.getTeam1().getId())
                    match.setResult(resultadoNuevo + " / ");
                else
                    match.setResult(" / " + resultadoNuevo);
            }
            entityManager.persist(match);

        } catch (Exception e) {
            log.info("EXCEPTION ENVIANDO RESULTADOS: ", e);
        }
        // PROBLEMAS CUANDO SE ACTUALIZA PERO NO CAMBIA EL MATCHID
        entityManager.flush();
        return ResponseEntity.ok(model);
    }
}
