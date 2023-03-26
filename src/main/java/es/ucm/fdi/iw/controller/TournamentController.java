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

import java.io.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.web.servlet.view.RedirectView;
import es.ucm.fdi.iw.model.Team;
import es.ucm.fdi.iw.model.TeamMember;

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
    public String index(@PathVariable long id, Model model, HttpSession session) {
        // User target = entityManager.find(User.class, id);
        // model.addAttribute("user", target);
		// Team coachingTeam = new Team();
		// coachingTeam.setName("No team registered");
		// try{
		// 	model.addAttribute("coachingTeam", 
		// 	(Team)entityManager.createQuery("select t from Team t join TeamMember tm on t.id = tm.team.id where tm.user.id = :id").setParameter("id", id).getSingleResult());
		// }catch(Exception e){
		// 	model.addAttribute("coachingTeam", coachingTeam);
		// }
        return "join";
    }


}
