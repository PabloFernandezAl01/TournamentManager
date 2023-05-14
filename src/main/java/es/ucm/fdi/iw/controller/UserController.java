package es.ucm.fdi.iw.controller;
import es.ucm.fdi.iw.model.Transferable;
import es.ucm.fdi.iw.model.TeamMember;
import es.ucm.fdi.iw.model.Tournament;
import es.ucm.fdi.iw.model.User.Role;
import es.ucm.fdi.iw.model.Message;
import es.ucm.fdi.iw.model.Match;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Team;
import es.ucm.fdi.iw.LocalData;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
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
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.persistence.NoResultException;
import javax.persistence.EntityManager;
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
import com.fasterxml.jackson.databind.node.ObjectNode;
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

	// @Autowired
	// private SimpMessagingTemplate messagingTemplate;

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
		model.addAttribute("user", user);

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
				// Avisar de que la constraseña es igual a la anterior (Usuario bobo)
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
		if (requester.getId() != target.getId() &&
				!requester.hasRole(Role.ADMIN)) {
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

	/*
	 * Creacion de equipos
	 */
	@PostMapping("{id}/createTeam")
	@Transactional
	public String postCreateTeam(@PathVariable long id, HttpServletRequest request, Model model) throws Exception {

		String name = request.getParameter("name");
		User u = entityManager.find(User.class, id);

		Team t = new Team();

		TeamMember tm = new TeamMember();

		// t.setCoach(u);
		// t.setName(name);

		// tm.setRole(RoleInTeam.COACH);
		// tm.setTeam(t);
		// tm.setUser(u);

		entityManager.persist(t);
		entityManager.persist(tm);
		entityManager.flush();

		model.addAttribute("user", u);

		return "user";
	}


	/**
	 * Posts a message to a match.
	 * 
	 * @param id of target user (source user is from ID)
	 * @param o  JSON-ized message, similar to {"message": "text goes here"}
	 * @throws JsonProcessingException
	 */
	@PostMapping("sendMsg/{userId}/{matchId}")
	@Transactional
	@ResponseBody
	public String sendMessage(@PathVariable long userId, @PathVariable long matchId,
			@RequestBody JsonNode node, Model model, HttpSession session)
			throws JsonProcessingException {

		// String text = node.get("message").asText();
		// if(text == "")
		// 	return "{\"result\": \"message not sent, empty string received.\"}";


		// User user = entityManager.find(User.class, userId);
		// Match match = entityManager.find(Match.class, matchId);
		
		// List<User> recipients = entityManager
		// .createQuery("select t.user from TeamMember t where t.team.id = :team1Id OR t.team.id = :team2Id",
		// 		User.class)
		// .setParameter("team1Id", match.getTeam1().getId())
		// .setParameter("team2Id", match.getTeam2().getId())
		// .getResultList();


		// for (User recipient : recipients) {

		// 	Team team = getUserTeamFromMatch(user, match);
		// 	Message m = new Message();
			
		// 	m.setRecipient(recipient);
		// 	m.setSender(user);
		// 	m.setDateSent(LocalDateTime.now());
		// 	m.setText(text);
		// 	m.setMatch(match);
		// 	m.setIamSender(true);
		// 	m.setSenderTeamName(team.getName());

		// 	entityManager.persist(m);

		// 	ObjectMapper mapper = new ObjectMapper();

		// 	String json = mapper.writeValueAsString(m.toTransfer());

		// 	log.info("Sending a message to {} with contents '{}'", userId, json);

		// 	messagingTemplate.convertAndSend("/user/" + recipient.getUsername() + "/queue/updates", json);
		// }
		
		// entityManager.flush(); // to get Id before commit

		return "{\"result\": \"message sent.\"}";
	}

	/**
	 * Returns JSON with all received messages
	 */
	@GetMapping(path = "received", produces = "application/json")
	@Transactional // para no recibir resultados inconsistentes
	@ResponseBody // para indicar que no devuelve vista, sino un objeto (jsonizado)
	public List<Message.Transfer> retrieveMessages(HttpSession session) {
		long userId = ((User) session.getAttribute("u")).getId();

		User user = entityManager.find(User.class, userId);

		log.info("Generating message list for user {} ({} messages)",
				user.getUsername(), user.getReceived().size());

		List<Message> received = new ArrayList<>();
		for(Message msg : user.getReceived()) {
			msg.setIamSender(msg.getSender().getId() == user.getId());
			received.add(msg);
		}

		return received.stream().map(Transferable::toTransfer).collect(Collectors.toList());
	}

	private Team getUserTeamFromMatch(User user, Match match) {
		return null;
        // try {
        //     Team team = entityManager.createQuery(
        //             "SELECT m.team FROM TeamMember m WHERE (m.team.id = :matchTeam1 OR m.team.id = :matchTeam2) AND m.user.id = :userId",
        //             Team.class)
        //             .setParameter("matchTeam1", match.getTeam1().getId())
        //             .setParameter("matchTeam2", match.getTeam2().getId())
		// 			.setParameter("userId", user.getId())
        //             .getSingleResult();
        //     return team;

        // } catch (NoResultException e) {
        //     return null;
        // }
    }
}
