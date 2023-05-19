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
                "SELECT t.team FROM TeamMember t WHERE t.user.id = :userId AND t.isCoach = true")
                .setParameter("userId", userId).getSingleResult();
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

    class IntWrapper {
        int value;
    }
    class MatchWrapper {
        Match value;
    }

    @GetMapping("/{tournamentId}/match/{matchId}")
    @Transactional
    public String match(HttpSession session, @PathVariable long tournamentId, @PathVariable long matchId, Model model) {

        Tournament t = entityManager.find(Tournament.class, tournamentId);
        Match m = entityManager.find(Match.class, matchId);

        model.addAttribute("tournament", t);
        model.addAttribute("match", m);


        model.addAttribute("coachingTeam", userCoachingTeam(session) );

        return "match";
    }

 /*
     * Devuelve un bool indicando si el usuario es coach de algun equipo
     */
    private Team userCoachingTeam(HttpSession session) {

        User user = null;
        try {
            // Obtiene la informacion del usuario de la sesion actual
            user = (User) session.getAttribute("u");
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
        }

        try{
            Team coachingTeam = (Team) entityManager.createQuery(
            "SELECT t.team FROM TeamMember t WHERE t.user.id = :userId and t.isCoach = true", Team.class)
            .setParameter("userId", user.getId())
            .getSingleResult();

            return coachingTeam;
        }
        catch (Exception e) {
            return null;
        }
    }

    private Map<Integer, List<Match>> getPartidosPorRonda(List<Match> matches, IntWrapper maxRound, MatchWrapper lastMatch) {
        Map<Integer, List<Match>> partidosPorRonda = new HashMap<>();

            List<Match> partidosEnRonda = new ArrayList<>();

            for (Match match : matches) {

                int ronda = match.getRoundNumber();

                log.info("ronda: " + ronda);
                if (ronda >= maxRound.value)
                    maxRound.value = ronda;

                lastMatch.value = match;

                //Variable auxiliar para guardar los partidos que hay actualmente en el map
                partidosEnRonda = partidosPorRonda.getOrDefault(ronda, new ArrayList<>());
                partidosEnRonda.add(match);

                partidosPorRonda.put(ronda, partidosEnRonda);
            }

            return partidosPorRonda;
    }

    @GetMapping("{tournamentId}/bracket")
    @Transactional
    public String bracket(HttpSession session, @PathVariable long tournamentId, Model model) {

        model.addAttribute("ongoing", Boolean.TRUE);

        try {
            Tournament tournament = (Tournament) entityManager.createQuery(
                    "SELECT t FROM Tournament t WHERE t.id = :tournamentid", Tournament.class)
                    .setParameter("tournamentid", tournamentId)
                    .getSingleResult();        
        
            int maxRound = 0;
            Match lastMatch = null;

            //Lista de los partidos
            List<Match> matches = entityManager.createQuery(
                    "SELECT m FROM Match m WHERE m.tournament.id = :tournamentid", Match.class)
                    .setParameter("tournamentid", tournamentId)
                    .getResultList();


            IntWrapper maxRoundWrapper = new IntWrapper();
            MatchWrapper lastMatchWrapper = new MatchWrapper();

            //Vemos cuantos partidos hay por ronda, cogemos cual es la maxima ronda y el ultimo partido
            Map<Integer, List<Match>> partidosPorRonda = getPartidosPorRonda(matches, maxRoundWrapper, lastMatchWrapper);
            maxRound = maxRoundWrapper.value;
            lastMatch = lastMatchWrapper.value;
        
            log.info("MaxRound: " + maxRound);
             
            // SI ES EL ULTIMO PARTIDO
            if (maxRound == tournament.getRounds() - 1) {
                if (lastMatch.getWinner() != null) {
                    tournament.setStatus(TournamentStatus.FINISHED);
                }
            }

            // SI NO ES EL ULTIMO PARTIDO
            else {
            
                List<Team> winners = new ArrayList<>();
                boolean allMatchesFinished = true;
                for (Match partido : matches) {
                    if (partido.getWinner() == null)
                        allMatchesFinished = false;
                    else
                        winners.add(partido.getWinner());
                }

                // SI LA JORNADA ANTERIOR HA ACABADO CREAMOS NUEVOS PARTIDOS
                if (allMatchesFinished) {
                    if (tournament.getType() == 0) {
                        createMatches(tournament, maxRound + 1, winners);
                    }
                    else {
                        createMatchesLeague(tournament, maxRound);
                    }

                    matches = entityManager.createQuery(
                            "SELECT m FROM Match m WHERE m.tournament.id = :tournamentid", Match.class)
                            .setParameter("tournamentid", tournamentId)
                            .getResultList();

                    partidosPorRonda = getPartidosPorRonda(matches, maxRoundWrapper, lastMatchWrapper);
                    maxRound = maxRoundWrapper.value;
                    lastMatch = lastMatchWrapper.value;
                }
            }
           
            model.addAttribute("tournament", tournament);
            model.addAttribute("partidosPorRonda", partidosPorRonda);
            model.addAttribute("lastRound", maxRound);
            model.addAttribute("lastMatch", lastMatch);
            model.addAttribute("tournamentTopic", tournament.getMessageTopic().getTopicId());
            model.addAttribute("userInTournament", isUserInTournament(tournament, session));

            User u = (User) session.getAttribute("u");

            // INSERTAR TOPICSIDS DE USUARIO
            List<Tournament> tournaments = getAllUserTournaments(u);
            List<Match> matchestopics = getAllUserMatches(u);

            String topics = String.join(",", getAllTopicIds(tournaments, matchestopics));
            session.setAttribute("topics", topics);
            log.info("Topics for {} are {}", u.getUsername(), topics);

            if (tournament.getType() == 0) {
                return "bracket";

            } else {

                List<TournamentTeam> teamsList = entityManager.createQuery(
                        "SELECT t FROM TournamentTeam t WHERE t.tournament.id = :tournamentid ORDER BY t.puntuacion DESC",
                        TournamentTeam.class)
                        .setParameter("tournamentid", tournamentId)
                        .getResultList();

                model.addAttribute("teamsList", teamsList);

                return "tableBracket";
            }

        } catch (Exception e) {
            log.info("HA SALTADO UNA EXCEPCION: ", e);
        }
        return "bracket";
    }

    private boolean isUserInTournament(Tournament tournament, HttpSession session) {
        
        User user = (User) session.getAttribute("u");

        Team userTeam = user.getTeam();

        try{
            entityManager.createQuery(
                "SELECT t FROM TournamentTeam t WHERE t.tournament.id = :tournamentid and t.team.id = :teamId ",
                TournamentTeam.class)
                .setParameter("tournamentid", tournament.getId())
                .setParameter("teamId", userTeam.getId())
                .getSingleResult();

            return true;
        }
        catch(Exception e) {
            return false;
        }
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

        return new RedirectView("/join");
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

    @PostMapping("sendResults/{tournamentId}/{matchId}/{teamId}")
    @Transactional
    public String sendResults(@RequestParam("resultadoTeam1") int resultadoTeam1,
            @RequestParam("resultadoTeam2") int resultadoTeam2, HttpSession session, @PathVariable long tournamentId,
            @PathVariable long matchId, @PathVariable long teamId, Model model) {
        try {
            Match match = entityManager
                    .createQuery("select m from Match m where m.id = :matchId AND m.tournament.id = :tournamentId ",
                            Match.class)
                    .setParameter("matchId", matchId)
                    .setParameter("tournamentId", tournamentId)
                    .getSingleResult();

            Tournament tournament = entityManager.find(Tournament.class, tournamentId);

            String resultadoNuevo = resultadoTeam1 + " - " + resultadoTeam2;
           
            if(teamId == match.getTeam1().getId()) {
                match.setResultTeam1(resultadoNuevo);
            }
            else {
                match.setResultTeam2(resultadoNuevo);
            }

            if(match.getResultTeam1().equals(match.getResultTeam2())){
                String[] numbers = resultadoNuevo.split(" - ");

                int number1 = Integer.parseInt(numbers[0]); 
                int number2 = Integer.parseInt(numbers[1]); 

                if(number1 > number2) {
                    match.setWinner(match.getTeam1());
                    
                    if(tournament.getType() == 1) {
                        setLeagueMatchWinner(match.getTeam1(), match.getTeam2(), tournament);
                    }
                }
                else if(number2 > number1){
                    match.setWinner(match.getTeam2());

                    if(tournament.getType() == 1) {
                        setLeagueMatchWinner(match.getTeam2(), match.getTeam1(), tournament);
                    }
                }
            }

            entityManager.persist(match);

        } catch (Exception e) {
            log.info("EXCEPTION ENVIANDO RESULTADOS: ", e);
        }
        // PROBLEMAS CUANDO SE ACTUALIZA PERO NO CAMBIA EL MATCHID
        entityManager.flush();
        
        return "redirect:/tournament/" + tournamentId + "/match/" + matchId;
    }

    @PostMapping("sendResults/setWinner/{tournamentId}/{matchId}/{teamId}")
    @Transactional
    public String setMatchWinner(HttpSession session, @PathVariable long tournamentId,
            @PathVariable long matchId, @PathVariable long teamId, Model model) {
        try {
            Match match = entityManager
                    .createQuery("select m from Match m where m.id = :matchId AND m.tournament.id = :tournamentId ",
                            Match.class)
                    .setParameter("matchId", matchId)
                    .setParameter("tournamentId", tournamentId)
                    .getSingleResult();
           
            match.setResultTeam1("Intervenido por administrador");
            match.setResultTeam2("Intervenido por administrador");

            Tournament tournament = entityManager.find(Tournament.class, tournamentId);

            if(teamId == match.getTeam1().getId()){
                match.setWinner(match.getTeam1());

                if(tournament.getType() == 1) {
                    setLeagueMatchWinner(match.getTeam1(), match.getTeam2(), tournament);
                }
            }
            else {
                match.setWinner(match.getTeam2());

                if(tournament.getType() == 1) {
                    setLeagueMatchWinner(match.getTeam2(), match.getTeam1(), tournament);
                }
            }

            entityManager.persist(match);

        } catch (Exception e) {
            log.info("EXCEPTION ENVIANDO RESULTADOS: ", e);
        }
        // PROBLEMAS CUANDO SE ACTUALIZA PERO NO CAMBIA EL MATCHID
        entityManager.flush();
        
        return "redirect:/tournament/" + tournamentId + "/match/" + matchId;
    }
    
    private void setLeagueMatchWinner(Team winner, Team loser, Tournament tournament) {
        TournamentTeam winnerTeam = entityManager.createQuery(
            "SELECT t FROM TournamentTeam t WHERE t.team.id = :teamid and t.tournament.id = :tournamentid ",
            TournamentTeam.class)
            .setParameter("tournamentid", tournament.getId())
            .setParameter("teamid", winner.getId())
            .getSingleResult();

        TournamentTeam loserTeam = entityManager.createQuery(
            "SELECT t FROM TournamentTeam t WHERE t.team.id = :teamid and t.tournament.id = :tournamentid ",
            TournamentTeam.class)
            .setParameter("tournamentid", tournament.getId())
            .setParameter("teamid", loser.getId())
            .getSingleResult();

            winnerTeam.setVictorias(winnerTeam.getVictorias() + 1);
            loserTeam.setDerrotas(loserTeam.getDerrotas() + 1);
            winnerTeam.setPuntuacion(winnerTeam.getPuntuacion() + 3);

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

	private List<Match> getAllUserMatches(User u) {

		if (u.getTeam() == null) {
			return new ArrayList<>();
		}
		List<Match> query = entityManager.createQuery(
				"SELECT e FROM Match e WHERE e.team1.id = :teamId OR e.team2.id = :teamId",
				Match.class).setParameter("teamId", u.getTeam().getId()).getResultList();

		for (Match m : query) {
			log.info("My team is {}, and one of my matches is {}", u.getTeam().getId(), m.getId());
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
