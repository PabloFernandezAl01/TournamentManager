package es.ucm.fdi.iw.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import es.ucm.fdi.iw.model.User;
import java.io.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import es.ucm.fdi.iw.model.Tournament;
import java.util.List;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.Map;


/**
 *  Non-authenticated requests only.
 */
@Controller
public class RootController {

	private static final Logger log = LogManager.getLogger(RootController.class);
    @Autowired
	private EntityManager entityManager;
    @Autowired
	private PasswordEncoder passwordEncoder;

    public String encodePassword(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

    

	@GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("home", Boolean.FALSE);
        model.addAttribute("create", Boolean.FALSE);
        model.addAttribute("join", Boolean.FALSE);
        model.addAttribute("onGoing", Boolean.FALSE);
        model.addAttribute("record", Boolean.FALSE);
        return "login";
    }

	@GetMapping("/")
    public String index(Model model) {
        model.addAttribute("home", Boolean.TRUE);
        model.addAttribute("create", Boolean.FALSE);
        model.addAttribute("join", Boolean.FALSE);
        model.addAttribute("onGoing", Boolean.FALSE);
        model.addAttribute("record", Boolean.FALSE);
        return "index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("home", Boolean.FALSE);
        model.addAttribute("create", Boolean.TRUE);
        model.addAttribute("join", Boolean.FALSE);
        model.addAttribute("onGoing", Boolean.FALSE);
        model.addAttribute("record", Boolean.FALSE);
        return "create";
    }

    @GetMapping("/join")
    public String join(Model model) {
        model.addAttribute("home", Boolean.FALSE);
        model.addAttribute("create", Boolean.FALSE);
        model.addAttribute("join", Boolean.TRUE);
        model.addAttribute("onGoing", Boolean.FALSE);
        model.addAttribute("record", Boolean.FALSE);
    
        List<Long> numTeams = new ArrayList<>();

        List<String> tournamentNames = new ArrayList<>();

        List<Tournament> results = new ArrayList<>();
        Long nTeams = 0L;

        Map<String, String> mapa = new HashMap<>();
        
        Object hola = new Object();

        results = entityManager.createQuery("select t from Tournament t", Tournament.class).getResultList();
        for (Tournament tournament : results) {
            long tid = tournament.getId();
           
            try {
                TypedQuery<Long> query = entityManager.createQuery("SELECT count(e.team) FROM Tournament_Team e WHERE e.tournament.id = :tournamentid", Long.class);
                nTeams = query.setParameter("tournamentid", tid).getSingleResult();

            }catch(Exception e){
                nTeams = 0L;
                
            }

            String auxTeams = new String(nTeams+"/"+tournament.getMaxTeams());
            mapa.put(tournament.getName(), auxTeams);
        }
        model.addAttribute("names_teams", mapa);

        return "join";
    }

    @GetMapping("/ongoing")
    public String ongoing(Model model) {
        model.addAttribute("home", Boolean.FALSE);
        model.addAttribute("create", Boolean.FALSE);
        model.addAttribute("join", Boolean.FALSE);
        model.addAttribute("onGoing", Boolean.TRUE);
        model.addAttribute("record", Boolean.FALSE);
        return "ongoing";
    }

    @GetMapping("/record")
    public String record(Model model) {
        model.addAttribute("home", Boolean.FALSE);
        model.addAttribute("create", Boolean.FALSE);
        model.addAttribute("join", Boolean.FALSE);
        model.addAttribute("onGoing", Boolean.FALSE);
        model.addAttribute("record", Boolean.TRUE);
        return "record";
    }

    @GetMapping("/bracket")
    public String bracket(Model model) {
        model.addAttribute("home", Boolean.FALSE);
        model.addAttribute("create", Boolean.FALSE);
        model.addAttribute("join", Boolean.TRUE);
        model.addAttribute("onGoing", Boolean.FALSE);
        model.addAttribute("record", Boolean.FALSE);
        return "bracket";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("home", Boolean.FALSE);
        model.addAttribute("create", Boolean.FALSE);
        model.addAttribute("join", Boolean.FALSE);
        model.addAttribute("onGoing", Boolean.FALSE);
        model.addAttribute("record", Boolean.FALSE);
        return "register";
    }

    /**
	 * Registrar usuario
	 */
	@PostMapping("/registUser")
	@Transactional
	public RedirectView registUser(@ModelAttribute User registered, 
		Model model) throws IOException {
        registered.setEnabled(true);
        registered.setRoles("USER");
        registered.setPassword(encodePassword(registered.getPassword()));
		entityManager.persist(registered);
		entityManager.flush(); 
		return new RedirectView("/login");
	}

    /**
	* Crear torneo
	*/
	@PostMapping("/createTournament")
	@Transactional
	public RedirectView createTournament(@ModelAttribute Tournament tournament, 
		Model model) throws Exception {
        tournament.setType("HOLA");
		tournament.setStatus("NOT_STARTED");
		entityManager.persist(tournament);
		entityManager.flush(); 
		return new RedirectView("/join");
	}
}
