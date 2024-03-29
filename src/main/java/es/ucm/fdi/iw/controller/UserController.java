package es.ucm.fdi.iw.controller;
import es.ucm.fdi.iw.model.Transferable;
import es.ucm.fdi.iw.model.TeamMember;
import es.ucm.fdi.iw.model.User.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import es.ucm.fdi.iw.model.Message;
import es.ucm.fdi.iw.model.Match;
import es.ucm.fdi.iw.model.Tournament;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Team;
import es.ucm.fdi.iw.LocalData;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.stream.Collectors;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Base64;
import java.util.List;
import java.io.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * User management.
 *
 * Access to this end-point is authenticated.
 */
@Controller()
@RequestMapping("user")
public class UserController {

	private static final Logger log = LogManager.getLogger(UserController.class);

	/*
     * Proporciona acceso a la base de BD desde cualquier parte del controlador
     */
	@Autowired
	private EntityManager entityManager;

	@Autowired
	private LocalData localData;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Exception to use when denying access to unauthorized users.
	 * 
	 * In general, admins are always authorized, but users cannot modify
	 * each other's profiles.
	 */
	@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "No eres administrador, y éste no es tu perfil") // 403
	public static class NoEsTuPerfilException extends RuntimeException {}

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

	/**
	 * Generates random tokens. From https://stackoverflow.com/a/44227131/15472
	 * 
	 * @param byteLength
	 * @return
	 */
	public static String generateRandomBase64Token(int byteLength) {
		SecureRandom secureRandom = new SecureRandom();
		byte[] token = new byte[byteLength];
		secureRandom.nextBytes(token);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(token); // base64 encoding
	}

	/**
	 * Landing page for a user profile
	 */
	@GetMapping("{id}")
	public String user(@PathVariable long id, Model model, HttpSession session) {
		log.warn("El usuario entra en su perfil");
		User user = entityManager.find(User.class, id);
		// User user = (User) session.getAttribute("u");
		log.info("ESTAMOS EN PERFIL " +  user.getUsername());
		model.addAttribute("user", user);

		model.addAttribute("allMatches", getAllMatches(user));

		return "user";
	}

	@GetMapping("getMessages/{id}")
	public String getMessages(@PathVariable long id, Model model, HttpSession session) {
		User user = entityManager.find(User.class, id);
		// User user = (User) session.getAttribute("u");
		log.info("ESTAMOS EN PERFIL " +  user.getUsername());
		model.addAttribute("user", user);

		model.addAttribute("allMatches", getAllMatches(user));

		return "user";
	}

	/**
	 * Alter or create a user
	 */
	@PostMapping("/{id}")
	@Transactional
	public String postUser(HttpServletResponse response, @PathVariable long id, @ModelAttribute User edited, 
		@RequestParam(required = false) String pass2, Model model, HttpSession session) throws IOException {

		User requester = (User) session.getAttribute("u");
		User target = null;

		if (id == -1 && requester.hasRole(Role.ADMIN)) {
			// create new user with random password
			target = new User();
			target.setPassword(encodePassword(generateRandomBase64Token(12)));
			target.setEnabled(true);
			entityManager.persist(target);
			entityManager.flush(); // forces DB to add user & assign valid id
			id = target.getId(); // retrieve assigned id from DB
		}

		// retrieve requested user
		target = entityManager.find(User.class, id);
		model.addAttribute("user", target);

		if (requester.getId() != target.getId() && !requester.hasRole(Role.ADMIN)) {
			throw new NoEsTuPerfilException();
		}

		if (edited.getPassword() != null) {
			if (!edited.getPassword().equals(pass2)) {
				// TODO: Avisar de que la constraseña es igual a la anterior
			} else {
				// save encoded version of password
				target.setPassword(encodePassword(edited.getPassword()));
			}
		}

		target.setUsername(edited.getUsername());
		target.setFirstName(edited.getFirstName());
		target.setLastName(edited.getLastName());

		// update user session so that changes are persisted in the session, too
		if (requester.getId() == target.getId()) {
			session.setAttribute("u", target);
		}

		return "user";
	}



	// ------------------------- Apartado de imagenes --------------------------

	/**
	 * Returns the default profile pic
	 * 
	 * @return
	 */
	private static InputStream defaultPic() {
		return new BufferedInputStream(Objects.requireNonNull(UserController.class.getClassLoader().getResourceAsStream("static/img/default-pic.jpg")));
	}

	/**
	 * Downloads a profile pic for a user id
	 * 
	 * @param id
	 * @return
	 * @throws IOException
	 */
	@GetMapping("{id}/pic")
	public StreamingResponseBody getPic(@PathVariable long id) throws IOException {
		File f = localData.getFile("user", "" + id + ".jpg");
		InputStream in = new BufferedInputStream(f.exists() ? new FileInputStream(f) : UserController.defaultPic());
		return os -> FileCopyUtils.copy(in, os);
	}

	/**
	 * Uploads a profile pic for a user id
	 * 
	 * @param id
	 * @return
	 * @throws IOException
	 */
	@PostMapping("{id}/pic")
	@ResponseBody
	public String setPic(@RequestParam("photo") MultipartFile photo, @PathVariable long id, HttpServletResponse response, HttpSession session, Model model) throws IOException {

		User target = entityManager.find(User.class, id);
		model.addAttribute("user", target);

		// check permissions
		User requester = (User) session.getAttribute("u");
		if (requester.getId() != target.getId() && !requester.hasRole(Role.ADMIN)) {
			throw new NoEsTuPerfilException();
		}

		log.info("Updating photo for user {}", id);
		File f = localData.getFile("user", "" + id + ".jpg");
		if (photo.isEmpty()) {
			log.info("failed to upload photo: emtpy file?");
		} else {
			try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
				byte[] bytes = photo.getBytes();
				stream.write(bytes);
				log.info("Uploaded photo for {} into {}!", id, f.getAbsolutePath());
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				log.warn("Error uploading " + id + " ", e);
			}
		}
		return "{\"status\":\"photo uploaded correctly\"}";
	}


	// ------------------------- Apartado de equipos ----------------------------

	/*
	 * Creacion de equipos
	 */
	@PostMapping("{id}/createTeam")
	@Transactional
	public RedirectView postCreateTeam(@PathVariable long id, HttpServletRequest request, Model model) throws Exception {

		// Usuario de la sesion
		User u = entityManager.find(User.class, id);

		// Nombre del equipo recogido del formulario
		String name = request.getParameter("name");

		// Se crea el nuevo equipo
		Team t = new Team();
		t.setName(name);

		// Se crea un nuevo miembro de equipo
		TeamMember member = new TeamMember();

		/*
		 * Se dan valor a los atributos del member
		 * - IsCoach: siempre es coach el usuario que crea el equipo
		 * - Team: El team creado
		 * - User: El user de la sesion
		 */
		member.setIsCoach(true);
		member.setTeam(t);
		member.setUser(u);

		// Se persisten ambos objetos
		entityManager.persist(t); entityManager.persist(member);

		model.addAttribute("user", u);

		return new RedirectView("teams");

	}

	/* Envio de mensajes por match */
	/**
	 * Posts a message to a match.
	 * 
	 * @param id of target user (source user is from ID)
	 * @param o  JSON-ized message, similar to {"message": "text goes here"}
	 * @throws JsonProcessingException
	 */
	@PostMapping("sendMsg/{tournamentId}")
	@Transactional
	@ResponseBody
	public String sendMessage(@PathVariable long tournamentId, @RequestBody JsonNode node, Model model, HttpSession session) throws JsonProcessingException {

		// Obtiene el contenido del mensaje
		String text = node.get("message").asText();
		if (text == "") return "{\"result\": \"message not sent, empty string received.\"}";

		// Lo envia al log
		log.info("Mensaje: " + text);
		
		// Obtiene el match y el usuario de a sesion
		Tournament tournament = entityManager.find(Tournament.class, tournamentId);
		User user = entityManager.find(User.class, ((User)session.getAttribute("u")).getId());
		
		// Obtiene el equipo del usuario a partir del Match
		Team team = user.getTeam();

		// Crea el mensaje
		Message m = new Message();
		m.setRecipient(tournament.getMessageTopic());
		m.setSender(user);
		m.setDateSent(LocalDateTime.now());
		m.setText(text);
		m.setIamSender(true);
		m.setSenderTeamName(team.getName());

		entityManager.persist(m);

		// Convierte el mensaje en JSON
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(m.toTransfer());

		log.info("Sending a message to {} with contents '{}'", tournament.getMessageTopic().getTopicId(), json);
																					
		// messagingTemplate.convertAndSend("/user/" + recipient.getUsername() + "/queue/updates", json);
		messagingTemplate.convertAndSend("/topic/" + tournament.getMessageTopic().getTopicId(), json);
		entityManager.flush(); // To get Id before commit

		return "{\"result\": \"message sent.\"}";
	}

	@PostMapping("sendMsg/match/{userMatchId}")
	@Transactional
	@ResponseBody
	public String sendMessageMatch(@PathVariable long userMatchId, @RequestBody JsonNode node, Model model, HttpSession session) throws JsonProcessingException {

		// Obtiene el contenido del mensaje
		String text = node.get("message").asText();
		if (text == "") return "{\"result\": \"message not sent, empty string received.\"}";

		// Lo envia al log
		log.info("Mensaje: " + text);
		
		// Obtiene el match y el usuario de a sesion
		Match match = entityManager.find(Match.class, userMatchId);
		User user = entityManager.find(User.class, ((User)session.getAttribute("u")).getId());
		
		// Obtiene el equipo del usuario a partir del Match
		Team team = user.getTeam();

		// Crea el mensaje
		Message m = new Message();
		m.setRecipient(match.getMessageTopic());
		m.setSender(user);
		m.setDateSent(LocalDateTime.now());
		m.setText(text);
		m.setIamSender(true);
		m.setSenderTeamName(team.getName());

		entityManager.persist(m);

		// Convierte el mensaje en JSON
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(m.toTransfer());

		log.info("Sending a message to {} with contents '{}'", match.getMessageTopic().getTopicId(), json);
																					
		// messagingTemplate.convertAndSend("/user/" + recipient.getUsername() + "/queue/updates", json);
		messagingTemplate.convertAndSend("/topic/" + match.getMessageTopic().getTopicId(), json);
		entityManager.flush(); // To get Id before commit

		return "{\"result\": \"message sent.\"}";
	}

	/**
	 * Returns JSON with all received messages
	 */
	@GetMapping(path = "rcvMsg/match/{matchId}", produces = "application/json")
	@Transactional // Para no recibir resultados inconsistentes
	@ResponseBody // Para indicar que no devuelve vista, sino un objeto (jsonizado)
	public List<Message.Transfer> recieveMessagesMatch(@PathVariable long matchId, HttpSession session) {

		// Obtiene el match y el usuario de a sesion
		Match match = entityManager.find(Match.class, matchId);
		User user = entityManager.find(User.class, ((User)session.getAttribute("u")).getId());

		List<Message> received = new ArrayList<>();
		List<Message> messages = match.getMessageTopic().getMessages();
		
		for(Message msg : messages) {
			msg.setIamSender(msg.getSender().getId() == user.getId());
			received.add(msg);
		}

		return received.stream().map(Transferable::toTransfer).collect(Collectors.toList());
	}

	/**
	 * Returns JSON with all received messages
	 */
	@GetMapping(path = "rcvMsg/tournament/{tournamentId}", produces = "application/json")
	@Transactional // Para no recibir resultados inconsistentes
	@ResponseBody // Para indicar que no devuelve vista, sino un objeto (jsonizado)
	public List<Message.Transfer> recieveMessagesTournament(@PathVariable long tournamentId, HttpSession session) {

		// Obtiene el match y el usuario de a sesion
		Tournament tournament = entityManager.find(Tournament.class, tournamentId);
		User user = entityManager.find(User.class, ((User)session.getAttribute("u")).getId());

		List<Message> received = new ArrayList<>();
		List<Message> messages = tournament.getMessageTopic().getMessages();
		
		for(Message msg : messages) {
			msg.setIamSender(msg.getSender().getId() == user.getId());
			received.add(msg);
		}

		return received.stream().map(Transferable::toTransfer).collect(Collectors.toList());
	}

	// ----------------- Vista Teams.html ----------------

	 /*
     * Clase para manejar la informacion de un team (par {Team, List<User> jugadores})
     */
    @Data
    @AllArgsConstructor
    public static class TeamData {
        Team t; // Team
		List<User> players;
		List<User> coachs;

		public boolean isFull() {
			return players.size() >= Team.MAX_PLAYERS_IN_TEAM;
		}
    }

	@GetMapping("{id}/teams")
    public String team(Model model, HttpSession session) {
        model.addAttribute("teams", "active");

		User user = (User) session.getAttribute("u");
		model.addAttribute("user", user);

		List<TeamData> teams = getUserTeams(session);
		model.addAttribute("Teams", teams);

        return "teams";
    }

	@PostMapping("{id}/addPlayer/{teamId}")
	@Transactional
	public RedirectView addPlayerToTeam(@PathVariable long id, @PathVariable long teamId, HttpServletRequest request, Model model) throws Exception {

		// Obtiene el Team a partir de su id como parametro
		Team t = entityManager.find(Team.class, teamId);

		// Nombre del jugador recogido del formulario
		String username = request.getParameter("name");

		// Consulta para obtener el usuario por su username
		User u = null;
		try {
			u = entityManager.createNamedQuery("UserByUsername", User.class)
								   .setParameter("username", username).getSingleResult();
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		// Si el usuario no se puede unir al equipo
		if (!isTeamJoinable(u, t)) return new RedirectView("/user/{id}/teams");

		// Se crea un nuevo miembro de equipo
		TeamMember member = new TeamMember();

		/*
		 * Se dan valor a los atributos del member
		 * - IsCoach: No es coach porque se estan añadiendo jugadores
		 * - Team: El team del coach
		 * - User: El user especificado con el nombre
		 */
		member.setIsCoach(false);
		member.setTeam(t);
		member.setUser(u);

		entityManager.persist(member);

		return new RedirectView("/user/{id}/teams");
	}

	/*
	 * Devuelve un bool indicando si un usuario puede unirse a un team
	 * El usuario debe no estar ya en el equipo y debe ser un nombre de usuario existente
	 */
	private Boolean isTeamJoinable(User u, Team t) {
		
		if (u == null) return false;

		List<User> members = null;

		// Obtiene los miembros de un equipo
		try {
			members = entityManager.createNamedQuery("MembersByTeam", User.class)
										  .setParameter("teamId", t.getId()).getResultList();
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		// Si el usuario coincide con alguno de los miembros del equipo, el usuario ya se encuentra en el equipo
		for (User m : members) {
			if (m.getId() == u.getId()) return false;
		}
		
		return true;
	}

	/*
     * Devuelve una lista con los equipos a los que pertence el usuario
     */
    private List<TeamData> getUserTeams(HttpSession session) {

		User user = (User) session.getAttribute("u");

		List<TeamData> teamsData = new ArrayList<>();

		List<Team> teams = new ArrayList<>();

		// Obtiene todos los equipos del usuario
		try {
			teams = entityManager.createNamedQuery("AllMemberTeams", Team.class).
 									  setParameter("userId", user.getId()).getResultList();
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage());
		}

		// Por cada equipo del usuario, obtiene la lista de jugadores y coachs en él
		for (Team t : teams) {
			// Jugadores
			List<User> players = entityManager.createNamedQuery("PlayersInTeam", User.class).
			setParameter("teamId", t.getId()).getResultList();

			// Coachs
			List<User> coachs = entityManager.createNamedQuery("CoachsInTeam", User.class).
			setParameter("teamId", t.getId()).getResultList();

			teamsData.add(new TeamData(t, players, coachs));
		}

        return teamsData;

    }
	
	private List<Match> getAllMatches(User user){
		try{
			List<Team> userTeams= new ArrayList<>();

			userTeams = entityManager.createQuery("select t.team from TeamMember t where t.user.id = :userId",Team.class)
			.setParameter("userId", user.getId())
			.getResultList();
			List<Match> userMatches = new ArrayList<>();
			for(Team team: userTeams){

				userMatches = entityManager.createQuery("select m from Match m where (m.team1.id = :teamId OR m.team2.id = :teamId)",Match.class)
				.setParameter("teamId", team.getId())
				.getResultList();
			}
			if(userMatches.isEmpty()){
				return null;
			}
			return userMatches;
		} catch (NoResultException e){
			return null;
		}
	}
}
