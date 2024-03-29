package es.ucm.fdi.iw.controller;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import es.ucm.fdi.iw.model.Message;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;

/**
 *  Site administration.
 *
 *  Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("admin")
public class AdminController {

    private static final Logger log = LogManager.getLogger(RootController.class);
    

	@GetMapping("/")
    @Transactional
    public String index(HttpSession session,Model model) {
        log.info("ESTAMOS EN ADMINUSERS");

        // Obtiene la informacion clave de los torneos
        List<User> users = getUsers(session);

        // Añade los torneos al modelo
        model.addAttribute("users", users);
        
        return "admin";
    }


    @PostMapping("/adminUsers/{userId}")
    @Transactional
    public RedirectView adminUsers(@PathVariable long userId) {
        log.info("EN USER ID");

        User user = entityManager.find(User.class, userId);

        log.info("ESTE USER ", user.getFirstName());

        if(user.isEnabled())
            user.setEnabled(false);
        else
            user.setEnabled(true);

        entityManager.persist(user);

        return new RedirectView("/admin/");
    }

    @GetMapping("/adminMessages/{userId}")
    @Transactional
    public String adminMessages(Model model, @PathVariable long userId) {

        User user = entityManager.find(User.class, userId);

        model.addAttribute("messages", getMessages(user));
        model.addAttribute("username", user.getUsername());
        
        return "admin_messages";
    }

    @Autowired
    private EntityManager entityManager;

    private List<User> getUsers(HttpSession session) {

        List<User> usersFinal = new ArrayList<>();
        List<User> user = new ArrayList<>();

        try {
            // Consulta a la DB para obtener todos los torneos
            user = entityManager.createNamedQuery("User.allUsers", User.class).getResultList();

        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }

        for (User t : user) {
            log.info("USER ACT", t.getUsername());
            if (!t.hasRole(Role.ADMIN))
                usersFinal.add(t);           

        }

        return usersFinal;
    }

    private List<Message> getMessages(User u) {

        List<Message> messages = new ArrayList<>();

        try {
            messages = entityManager.createQuery("SELECT m FROM Message m WHERE m.sender.id = :userId", Message.class)
            .setParameter("userId", u.getId())
            .getResultList();
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }

        return messages;
    }

}
