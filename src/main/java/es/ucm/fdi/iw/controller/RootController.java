package es.ucm.fdi.iw.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *  Non-authenticated requests only.
 */
@Controller
public class RootController {

	private static final Logger log = LogManager.getLogger(RootController.class);

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
}
